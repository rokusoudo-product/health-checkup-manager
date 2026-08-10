package com.rokusoudo.healthcheckup

import java.time.LocalDate
import kotlin.math.abs

/**
 * ML Kit が返す座標付きテキストセル（[OcrCell]）を行・列にクラスタリングして
 * 表構造（グリッド）を復元し、「項目名・値・単位」のリストに変換するパーサー。
 *
 * ## 背景（Issue #12）
 * 日本の健診結果表は「項目名 / 今回 / 前回 / 前々回 / 基準値」のように
 * 複数の列が横に並ぶ表形式であり、1行に複数の数値が並ぶ。
 * 行の文字列だけを空白区切りで解釈する方式では、どの数値がどの列に属するかを
 * 原理的に判定できない。ML Kit on-device には表構造認識APIが存在しないため、
 * `boundingBox`（座標）を用いた行・列のクラスタリングを自前で実装する。
 *
 * ## アルゴリズム
 * 1. セルをY座標（中心）でクラスタリングして行に分割する（[clusterRows]）
 * 2. 表全体のセルをX座標（left）でクラスタリングし、共通の列アンカーを推定する（[clusterColumnAnchors]）
 * 3. 各行のセルを列アンカーに割り当て、行×列のグリッド（[buildGrid]）を復元する
 * 4. グリッドの各行を [OcrItem] に変換する（[rowToItem]）
 *
 * ## 列→項目のマッピング（暫定ルール）
 * 「今回」列の特定・項目名の正規化は別Issueで扱うため、現時点では
 * 「1列目を項目名、2列目を値」の暫定ルールで [OcrItem] に変換する。
 *
 * ## ヘッダー行の除外（Issue #13）
 * 「今回 / 前回 / 前々回」のように複数列を持つ健診結果表は、先頭行に
 * 検査日（例: 2025/8/22、R7.8.22）が列ごとに並ぶヘッダー行を持つことが多い。
 * このヘッダー行を項目データとして取り込まないよう、行内で日付として解釈できる
 * セルが2つ以上ある行を「ヘッダー行」と判定し、[OcrItem] への変換対象から除外する
 * （[isHeaderRow]）。単一のセルがたまたま日付に見えるだけではヘッダー行と
 * 判定しない（データ行の誤除外を避けるため）。
 * ヘッダー行から取得できた検査日は捨てず、値として採用する列（暫定ルールの2列目）
 * に対応する日付を [OcrParseResult.examDate] として呼び出し側に返す。
 *
 * ## 後方互換
 * 1行が単一セルのみで構成される場合（=旧方式のようにOCR行全体が1文字列のセルとして
 * 渡された場合）は、従来の正規表現ベースの1行パースにフォールバックし、
 * 項目名・値・単位を分離する。単一列の単純な健診表は、この経路で従来どおり動作する。
 */
object OcrParser {

    // 後方互換フォールバック用: 1セル内に「項目名(非空白) 数値 単位」が
    // まとめて含まれる場合の従来パターン。
    private val LEGACY_LINE_PATTERN = Regex("""(\S+)\s+([\d.]+)\s*([a-zA-Z/%]+)?""")

    // 行クラスタリング: 同一行とみなす閾値 = 平均セル高さ × この係数
    private const val ROW_Y_THRESHOLD_RATIO = 0.6

    // 列クラスタリング: 同一列とみなす閾値 = 平均セル幅 × この係数
    private const val COLUMN_X_THRESHOLD_RATIO = 0.75

    // 暫定ルール（Issue #12）: グリッドの2列目（インデックス1）を値として採用する。
    // ヘッダー行から検査日を取り出す際も、同じ列に対応する日付を採用する。
    private const val VALUE_COLUMN_INDEX = 1

    // ヘッダー行判定: 行内でこの数以上のセルが日付として解釈できる場合にヘッダー行とみなす。
    // 1（単一セル）だと、値がたまたま日付に見えるデータ行を誤って落としてしまうため2以上とする。
    private const val HEADER_ROW_MIN_DATE_CELLS = 2

    /**
     * 座標付きセルのリストを解析してOcrItemリストを返す（後方互換API）。
     * ヘッダー行から検出した検査日が必要な場合は [parseWithDate] を使用する。
     *
     * @param cells ML Kit の `Text.Element` 相当の「文字列 + boundingBox」のセルのリスト
     * @return 解析結果のリスト。全セルが空、またはグリッド復元結果が空の場合は
     *         空のOcrItem1件を返し手動入力を促す。
     */
    fun parse(cells: List<OcrCell>): List<OcrItem> = parseWithDate(cells).items

    /**
     * 座標付きセルのリストを解析し、[OcrItem] のリストとヘッダー行から検出した
     * 検査日（取得できた場合）を返す（Issue #13）。
     *
     * - グリッドの各行についてヘッダー行判定（[isHeaderRow]）を行い、ヘッダー行は
     *   [OcrItem] への変換対象から除外する。
     * - ヘッダー行が複数ある場合は、最初に検査日を抽出できた行の値を採用する。
     *
     * @param cells ML Kit の `Text.Element` 相当の「文字列 + boundingBox」のセルのリスト
     * @return items: 解析結果のリスト（ヘッダー行を除く）。空の場合は空のOcrItem1件。
     *         examDate: ヘッダー行から取得した検査日（"YYYY-MM-DD"形式）。取得できなければnull。
     */
    fun parseWithDate(cells: List<OcrCell>): OcrParseResult {
        val grid = buildGrid(cells)

        val items = mutableListOf<OcrItem>()
        var examDate: String? = null
        for (row in grid) {
            if (isHeaderRow(row)) {
                if (examDate == null) {
                    examDate = extractExamDateFromHeaderRow(row)
                }
                continue
            }
            rowToItem(row)?.let { items.add(it) }
        }

        return OcrParseResult(
            items = items.ifEmpty { listOf(OcrItem("", "", "")) },
            examDate = examDate
        )
    }

    /**
     * 座標付きセルを行・列にクラスタリングし、行×列のグリッド（各セルのテキスト）として復元する。
     * 複数ページ（複数枚撮影）にまたがる場合、座標系はページごとに独立しているため
     * ページ単位でクラスタリングし、結果をページ順に連結する。
     *
     * グリッド復元結果そのものをテストで直接検証できるよう公開する。
     *
     * @param cells 座標付きセルのリスト
     * @return 行ごとの列テキストのリスト（列インデックスはページ内で共通）。
     *         空白のみのセルは無視する。全セルが空白の場合は空リストを返す。
     */
    fun buildGrid(cells: List<OcrCell>): List<List<String>> {
        val nonBlankCells = cells.filter { it.text.isNotBlank() }
        if (nonBlankCells.isEmpty()) return emptyList()

        return nonBlankCells.groupBy { it.page }
            .toSortedMap()
            .values
            .flatMap { pageCells -> buildGridForPage(pageCells) }
    }

    private fun buildGridForPage(cells: List<OcrCell>): List<List<String>> {
        val rows = clusterRows(cells)
        val columnAnchors = clusterColumnAnchors(cells)
        return rows.map { rowCells -> mapRowToColumns(rowCells, columnAnchors) }
    }

    /**
     * セルをY座標（中心）でクラスタリングして行に分割する。
     * 同一行とみなす閾値は、全セルの平均高さを基準に決定する。
     * 各行内はX座標（left）の昇順にソートする。
     */
    private fun clusterRows(cells: List<OcrCell>): List<List<OcrCell>> {
        val sorted = cells.sortedBy { it.centerY }
        val avgHeight = cells.map { it.height.coerceAtLeast(1) }.average()
        val threshold = avgHeight * ROW_Y_THRESHOLD_RATIO

        val rows = mutableListOf<MutableList<OcrCell>>()
        var currentRow = mutableListOf(sorted.first())
        for (i in 1 until sorted.size) {
            val cell = sorted[i]
            val gap = abs(cell.centerY - sorted[i - 1].centerY)
            if (gap <= threshold) {
                currentRow.add(cell)
            } else {
                rows.add(currentRow)
                currentRow = mutableListOf(cell)
            }
        }
        rows.add(currentRow)

        return rows.map { row -> row.sortedBy { it.left } }
    }

    /**
     * 表全体（1ページ分）のセルをX座標（left）でクラスタリングし、
     * 共通の列境界（アンカー = クラスタ内leftの平均値）を推定する。
     * 同一列とみなす閾値は、全セルの平均幅を基準に決定する。
     *
     * @return X座標昇順の列アンカーのリスト
     */
    private fun clusterColumnAnchors(cells: List<OcrCell>): List<Double> {
        val sorted = cells.sortedBy { it.left }
        val avgWidth = cells.map { (it.right - it.left).coerceAtLeast(1) }.average()
        val threshold = avgWidth * COLUMN_X_THRESHOLD_RATIO

        val clusters = mutableListOf<MutableList<OcrCell>>()
        var current = mutableListOf(sorted.first())
        for (i in 1 until sorted.size) {
            val cell = sorted[i]
            val gap = abs(cell.left - sorted[i - 1].left)
            if (gap <= threshold) {
                current.add(cell)
            } else {
                clusters.add(current)
                current = mutableListOf(cell)
            }
        }
        clusters.add(current)

        return clusters.map { cluster -> cluster.map { it.left }.average() }
    }

    /**
     * 1行分のセルを、ページ全体の列アンカーに割り当ててグリッドの1行
     * （列数分のテキスト配列）に変換する。同じ列に複数セルが割り当てられた場合は
     * スペース区切りで連結する。
     */
    private fun mapRowToColumns(rowCells: List<OcrCell>, columnAnchors: List<Double>): List<String> {
        val columns = MutableList(columnAnchors.size) { StringBuilder() }
        for (cell in rowCells) {
            val columnIndex = columnAnchors.indices.minByOrNull { idx ->
                abs(columnAnchors[idx] - cell.left)
            } ?: 0
            if (columns[columnIndex].isNotEmpty()) {
                columns[columnIndex].append(" ")
            }
            columns[columnIndex].append(cell.text)
        }
        return columns.map { it.toString() }
    }

    /**
     * グリッドの1行を [OcrItem] に変換する。
     *
     * - 埋まっている列が1つだけの場合（=1行1セル相当。従来のOCRテキスト1行がそのまま
     *   1セルとして渡されたケース）は、従来の正規表現による項目名・値・単位の
     *   分離ロジックにフォールバックする（後方互換）。
     * - 埋まっている列が2つ以上の場合は、暫定ルール（1列目=項目名、2列目=値）を適用する。
     *   単位列の特定は別Issueで扱うため、この経路では単位は空文字列とする。
     */
    private fun rowToItem(row: List<String>): OcrItem? {
        val nonBlank = row.filter { it.isNotBlank() }
        if (nonBlank.isEmpty()) return null

        if (nonBlank.size == 1) {
            return parseLegacyLine(nonBlank.first())
        }

        val itemName = row.getOrNull(0)?.trim().orEmpty()
        val value = row.getOrNull(VALUE_COLUMN_INDEX)?.trim().orEmpty()
        if (itemName.isBlank() && value.isBlank()) return null

        return OcrItem(itemName = itemName, value = value, unit = "")
    }

    /**
     * 従来の1セル1文字列パターン（項目名+数値+単位）でパースする後方互換フォールバック。
     * マッチしない場合はnullを返す（呼び出し側で行スキップとして扱う）。
     */
    private fun parseLegacyLine(line: String): OcrItem? {
        if (line.isBlank()) return null
        val match = LEGACY_LINE_PATTERN.find(line) ?: return null
        val (itemName, value, unit) = match.destructured
        return OcrItem(itemName = itemName, value = value, unit = unit)
    }

    // ------------------------------------------------------------
    // ヘッダー行判定・検査日抽出（Issue #13）
    // ------------------------------------------------------------

    /**
     * 行内でこの数以上のセルが日付として解釈できる場合、その行をヘッダー行とみなす。
     * 単一セルが日付に見えるだけではヘッダー行と判定しない
     * （数値のみのデータ行を誤って除外しないため）。
     */
    private fun isHeaderRow(row: List<String>): Boolean {
        val dateCellCount = row.count { parseDateCell(it) != null }
        return dateCellCount >= HEADER_ROW_MIN_DATE_CELLS
    }

    /**
     * ヘッダー行から、値として採用する列（[VALUE_COLUMN_INDEX]）に対応する検査日を取り出す。
     * その列が日付として解釈できない場合はnullを返す（従来どおり手動入力に委ねる）。
     *
     * @return "YYYY-MM-DD" 形式の日付文字列。取得できなければnull。
     */
    private fun extractExamDateFromHeaderRow(row: List<String>): String? {
        val valueColumnText = row.getOrNull(VALUE_COLUMN_INDEX) ?: return null
        val date = parseDateCell(valueColumnText) ?: return null
        return "%04d-%02d-%02d".format(date.year, date.monthValue, date.dayOfMonth)
    }

    // 西暦4桁（例: 2025/8/22, 2025-08-22, 2025.8.22）
    private val SEIREKI_YEAR4_PATTERN = Regex("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$""")

    // 西暦2桁（例: 25/8/22）。健診結果表は近年の日付のため 2000年代とみなす。
    private val SEIREKI_YEAR2_PATTERN = Regex("""^(\d{2})[-/.](\d{1,2})[-/.](\d{1,2})$""")

    // 和暦（例: R7.8.22, H31.4.1）。元号の頭文字（R/H/S/M）+ 年 + 区切り + 月 + 区切り + 日。
    private val WAREKI_PATTERN = Regex("""^([RHSMrhsm])(\d{1,2})[-/.](\d{1,2})[-/.](\d{1,2})$""")

    /**
     * セルのテキストを日付として解釈できる場合は [LocalDate] を返す。
     * 西暦4桁・西暦2桁・和暦（令和/平成/昭和/明治）の区切り文字違い（"/", "-", "."）に対応する。
     * 解釈できない、または存在しない日付（例: 2月30日）の場合はnullを返す。
     */
    private fun parseDateCell(text: String): LocalDate? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        parseWareki(trimmed)?.let { return it }
        parseSeirekiYear4(trimmed)?.let { return it }
        parseSeirekiYear2(trimmed)?.let { return it }
        return null
    }

    private fun parseWareki(text: String): LocalDate? {
        val match = WAREKI_PATTERN.matchEntire(text) ?: return null
        val (era, warekiYearStr, monthStr, dayStr) = match.destructured
        val seirekiYear = toSeirekiYear(era.uppercase()[0], warekiYearStr.toInt()) ?: return null
        return toLocalDateOrNull(seirekiYear, monthStr.toInt(), dayStr.toInt())
    }

    private fun parseSeirekiYear4(text: String): LocalDate? {
        val match = SEIREKI_YEAR4_PATTERN.matchEntire(text) ?: return null
        val (yearStr, monthStr, dayStr) = match.destructured
        return toLocalDateOrNull(yearStr.toInt(), monthStr.toInt(), dayStr.toInt())
    }

    private fun parseSeirekiYear2(text: String): LocalDate? {
        val match = SEIREKI_YEAR2_PATTERN.matchEntire(text) ?: return null
        val (yearStr, monthStr, dayStr) = match.destructured
        return toLocalDateOrNull(2000 + yearStr.toInt(), monthStr.toInt(), dayStr.toInt())
    }

    /** 和暦の元号1文字と和暦年から西暦年を求める。未知の元号の場合はnull。 */
    private fun toSeirekiYear(era: Char, warekiYear: Int): Int? = when (era) {
        'R' -> 2018 + warekiYear // 令和1年 = 2019年
        'H' -> 1988 + warekiYear // 平成1年 = 1989年
        'S' -> 1925 + warekiYear // 昭和1年 = 1926年
        'M' -> 1867 + warekiYear // 明治1年 = 1868年
        else -> null
    }

    /** 実在しない日付（2月30日など）の場合はnullを返す。 */
    private fun toLocalDateOrNull(year: Int, month: Int, day: Int): LocalDate? =
        runCatching { LocalDate.of(year, month, day) }.getOrNull()
}

/**
 * OCR解析結果の1行分を表すデータクラス。
 *
 * @param itemName 項目名（例: "血圧(収縮期)"）
 * @param value    数値文字列（例: "120"）
 * @param unit     単位（例: "mmHg"）。未検出の場合は空文字列。
 */
data class OcrItem(
    val itemName: String,
    val value: String,
    val unit: String
)

/**
 * [OcrParser.parseWithDate] の結果を表すデータクラス（Issue #13）。
 *
 * @param items    ヘッダー行を除いた [OcrItem] のリスト
 * @param examDate ヘッダー行から取得した検査日（"YYYY-MM-DD"形式）。取得できなければnull。
 */
data class OcrParseResult(
    val items: List<OcrItem>,
    val examDate: String?
)
