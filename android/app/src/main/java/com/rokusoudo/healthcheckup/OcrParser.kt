package com.rokusoudo.healthcheckup

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
 * 本Issueの担当範囲はグリッド復元までとする。ヘッダー行の除外・「今回」列の特定・
 * 項目名の正規化は別Issueで扱うため、現時点では「1列目を項目名、2列目を値」の
 * 暫定ルールで [OcrItem] に変換する。
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

    /**
     * 座標付きセルのリストを解析してOcrItemリストを返す。
     *
     * @param cells ML Kit の `Text.Element` 相当の「文字列 + boundingBox」のセルのリスト
     * @return 解析結果のリスト。全セルが空、またはグリッド復元結果が空の場合は
     *         空のOcrItem1件を返し手動入力を促す。
     */
    fun parse(cells: List<OcrCell>): List<OcrItem> {
        val grid = buildGrid(cells)
        val results = grid.mapNotNull { row -> rowToItem(row) }
        return results.ifEmpty { listOf(OcrItem("", "", "")) }
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
        val value = row.getOrNull(1)?.trim().orEmpty()
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
