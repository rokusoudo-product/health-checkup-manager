package com.rokusoudo.healthcheckup

import java.time.LocalDate

/**
 * [OcrParser.buildGrid] が復元したグリッドから、値として採用すべき列（「今回」列）を
 * 特定するセレクタ（Issue #14）。
 *
 * ## 背景
 * 日本の健診結果表は「項目名 / 今回 / 前回 / 前々回 / 基準値」のように複数列を持つ
 * 経年比較型の表が事実上の標準である。[OcrParser] は現時点でグリッドの2列目を
 * 無条件に値として採用する暫定ルールで動いており（Issue #12 のコメント参照）、
 * どの列が「今回」かを判定していない。
 *
 * 本ファイルはその判定ロジックを [OcrParser] の既存関数に一切手を入れずに追加する
 * （並行して進行中の Issue #13 とのマージ競合を避けるため、意図的に新規ファイルとして
 * 分離している）。
 *
 * ## アルゴリズム
 * 1. グリッド内で「日付として解釈できるセルが2つ以上ある行」を探し、見つかれば
 *    その行を検査日ヘッダー行とみなす（[findHeaderRowIndex]）。複数見つかった場合は
 *    最初の行を採用する
 * 2. 項目名列（列0）を除く各列について、範囲表記（`3.5〜7.0` 等）や
 *    「以下」「未満」「以上」「超」を含む列、またはヘッダー行のラベルに
 *    「基準」「参考」を含む列は参考基準値列とみなし候補から除外する（[isReferenceRangeColumn]）
 * 3. ヘッダー行が見つかり、残った候補列すべてについて重複のない日付が判明した場合は、
 *    最も新しい日付の列を「今回」列として自動採用する
 * 4. 日付で判別できない場合、候補列が1列だけならそれを採用する。2列以上残る、または
 *    候補が0列の場合は自動判定失敗とし、呼び出し側にユーザー選択を促す
 *
 * ## Issue #13 との関係
 * ヘッダー行・日付の判定ロジックは Issue #13（`isHeaderRow` / `parseDateCell` 相当）と
 * 目的が重複するが、両Issueは並行実装のため本ファイルは意図的に自己完結させている。
 * マージ時に日付パース処理の共通化を検討する余地がある（対応は代表判断）。
 *
 * ## スコープ外
 * 前回・前々回の値を過去の記録として一括登録する機能は本Issueのスコープ外。
 * 「今回」列1列を正しく取り込むところまでを対象とする。
 */
object OcrColumnSelector {

    /** 項目名として扱う列インデックス（[OcrParser] の暫定ルールと同じ前提）。 */
    private const val ITEM_NAME_COLUMN_INDEX = 0

    /** 単一列（項目名+値1列）の健診表で採用する値列インデックス（従来の暫定ルールと同じ）。 */
    private const val DEFAULT_VALUE_COLUMN_INDEX = 1

    /** 列選択UIのプレビューに表示する最大項目数。 */
    private const val PREVIEW_ITEM_COUNT = 3

    /** ヘッダー行判定: 行内でこの数以上のセルが日付として解釈できる場合にヘッダー行とみなす。 */
    private const val HEADER_ROW_MIN_DATE_CELLS = 2

    // 範囲表記（例: "3.5〜7.0", "3.5-7.0"）。日付文字列（"2025-08-22"等）を誤って
    // マッチさせないよう、行全体に完全一致する場合のみ範囲表記とみなす（アンカー必須）。
    private val RANGE_PATTERN = Regex("""^[\d.]+\s*[~〜～\-–—]\s*[\d.]+$""")
    private val RANGE_KEYWORDS = listOf("以下", "未満", "以上", "超")
    private val REFERENCE_HEADER_KEYWORDS = listOf("基準", "参考")

    // 西暦4桁（例: 2025/8/22, 2025-08-22, 2025.8.22）
    private val SEIREKI_YEAR4_PATTERN = Regex("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$""")

    // 西暦2桁（例: 25/8/22）。健診結果表は近年の日付のため 2000年代とみなす。
    private val SEIREKI_YEAR2_PATTERN = Regex("""^(\d{2})[-/.](\d{1,2})[-/.](\d{1,2})$""")

    // 和暦（例: R7.8.22, H31.4.1）。元号の頭文字（R/H/S/M）+ 年 + 区切り + 月 + 区切り + 日。
    private val WAREKI_PATTERN = Regex("""^([RHSMrhsm])(\d{1,2})[-/.](\d{1,2})[-/.](\d{1,2})$""")

    /**
     * グリッドから値として採用する列を判定する。
     *
     * @param grid [OcrParser.buildGrid] の出力
     * @return 自動判定できれば [ColumnSelectionResult.Resolved]、判定できなければ
     *         列候補付きの [ColumnSelectionResult.NeedsUserSelection]
     */
    fun selectValueColumn(grid: List<List<String>>): ColumnSelectionResult {
        val columnCount = grid.maxOfOrNull { it.size } ?: 0
        if (columnCount <= DEFAULT_VALUE_COLUMN_INDEX + 1) {
            // 項目名列のみ、または値列が高々1つ（単一列の健診表）→ 列選択不要
            return ColumnSelectionResult.Resolved(DEFAULT_VALUE_COLUMN_INDEX)
        }

        val headerRowIndex = findHeaderRowIndex(grid)
        val valueColumns = (ITEM_NAME_COLUMN_INDEX + 1 until columnCount).toList()
        val candidateColumns = valueColumns.filterNot { isReferenceRangeColumn(grid, it, headerRowIndex) }

        if (candidateColumns.size == 1) {
            return ColumnSelectionResult.Resolved(candidateColumns.first())
        }
        if (candidateColumns.isEmpty()) {
            // 全列が基準値列と判定された場合は自動判定失敗として扱い、除外前の全列から選ばせる
            return ColumnSelectionResult.NeedsUserSelection(buildCandidates(grid, valueColumns, headerRowIndex))
        }

        if (headerRowIndex != null) {
            val columnDates = candidateColumns.associateWith { detectColumnDate(grid, headerRowIndex, it) }
            val allDatesResolved = columnDates.values.all { it != null }
            val distinctDates = columnDates.values.filterNotNull().distinct()
            if (allDatesResolved && distinctDates.size == columnDates.size) {
                val latestColumn = columnDates.entries.maxByOrNull { it.value!! }!!.key
                return ColumnSelectionResult.Resolved(latestColumn)
            }
        }

        return ColumnSelectionResult.NeedsUserSelection(buildCandidates(grid, candidateColumns, headerRowIndex))
    }

    /**
     * グリッドを、指定した列インデックスを値列として [OcrItem] のリストに変換する。
     * ユーザーが列選択ダイアログで列を選んだ後、その列の値で確認画面の項目リストを
     * 再構築するために使用する。
     *
     * 行変換ルールは [OcrParser] の暫定ルール（列0=項目名、値列=指定インデックス。
     * 単一セル1行の場合は正規表現ベースの後方互換フォールバック）と同一だが、値列の
     * インデックスを固定値(1)ではなく引数で受け取れるようにした点のみが異なる。
     * ヘッダー行の除外は行わない（本Issueのスコープ外。Issue #13 の担当範囲）。
     */
    fun rebuildItems(grid: List<List<String>>, valueColumnIndex: Int): List<OcrItem> {
        val results = grid.mapNotNull { row -> rowToItem(row, valueColumnIndex) }
        return results.ifEmpty { listOf(OcrItem("", "", "")) }
    }

    private fun rowToItem(row: List<String>, valueColumnIndex: Int): OcrItem? {
        val nonBlank = row.filter { it.isNotBlank() }
        if (nonBlank.isEmpty()) return null

        if (nonBlank.size == 1) {
            return parseLegacyLine(nonBlank.first())
        }

        val itemName = row.getOrNull(ITEM_NAME_COLUMN_INDEX)?.trim().orEmpty()
        val value = row.getOrNull(valueColumnIndex)?.trim().orEmpty()
        if (itemName.isBlank() && value.isBlank()) return null

        return OcrItem(itemName = itemName, value = value, unit = "")
    }

    // 後方互換フォールバック用: 1セル内に「項目名(非空白) 数値 単位」が
    // まとめて含まれる場合の従来パターン（[OcrParser] のフォールバックと同一）。
    private val LEGACY_LINE_PATTERN = Regex("""(\S+)\s+([\d.]+)\s*([a-zA-Z/%]+)?""")

    private fun parseLegacyLine(line: String): OcrItem? {
        if (line.isBlank()) return null
        val match = LEGACY_LINE_PATTERN.find(line) ?: return null
        val (itemName, value, unit) = match.destructured
        return OcrItem(itemName = itemName, value = value, unit = unit)
    }

    /**
     * グリッド内で「日付として解釈できるセルが2つ以上ある行」を探し、見つかれば
     * その行のインデックスを返す。複数見つかった場合は最初の行を採用する。
     * タイトル行（例: "健康診断結果"）など日付を含まない行は無視される。
     */
    private fun findHeaderRowIndex(grid: List<List<String>>): Int? {
        return grid.indexOfFirst { row -> row.count { parseDateCell(it) != null } >= HEADER_ROW_MIN_DATE_CELLS }
            .takeIf { it >= 0 }
    }

    /**
     * 指定した列が参考基準値列かどうかを判定する。ヘッダー行を除くデータ行の中に
     * 範囲表記・キーワードを含むセルが1つでもあれば基準値列とみなす。
     * ヘッダー行のラベル自体に「基準」「参考」を含む場合も基準値列とみなす。
     */
    private fun isReferenceRangeColumn(grid: List<List<String>>, columnIndex: Int, headerRowIndex: Int?): Boolean {
        val headerLabel = headerRowIndex?.let { grid.getOrNull(it)?.getOrNull(columnIndex) }?.trim().orEmpty()
        if (REFERENCE_HEADER_KEYWORDS.any { headerLabel.contains(it) }) return true

        val dataCells = grid.filterIndexed { index, _ -> index != headerRowIndex }
            .mapNotNull { it.getOrNull(columnIndex)?.trim() }
            .filter { it.isNotBlank() }

        return dataCells.any { cell -> RANGE_PATTERN.matches(cell) || RANGE_KEYWORDS.any { cell.contains(it) } }
    }

    /** ヘッダー行の指定列のセルを日付として解釈する。解釈できなければnull。 */
    private fun detectColumnDate(grid: List<List<String>>, headerRowIndex: Int, columnIndex: Int): LocalDate? {
        val text = grid.getOrNull(headerRowIndex)?.getOrNull(columnIndex) ?: return null
        return parseDateCell(text)
    }

    /**
     * 列選択UIに提示する候補リストを構築する。プレビューはヘッダー行を除くデータ行から
     * 先頭 [PREVIEW_ITEM_COUNT] 件（空白セルを除く）を採用する。
     */
    private fun buildCandidates(
        grid: List<List<String>>,
        columnIndexes: List<Int>,
        headerRowIndex: Int?
    ): List<ColumnCandidate> {
        val previewSourceRows = grid.filterIndexed { index, _ -> index != headerRowIndex }
        return columnIndexes.map { col ->
            val headerLabel = headerRowIndex?.let { grid.getOrNull(it)?.getOrNull(col) }?.trim().orEmpty()
            val previews = previewSourceRows.mapNotNull { it.getOrNull(col)?.trim() }
                .filter { it.isNotBlank() }
                .take(PREVIEW_ITEM_COUNT)
            ColumnCandidate(columnIndex = col, headerLabel = headerLabel, previewValues = previews)
        }
    }

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

/** [OcrColumnSelector.selectValueColumn] の結果を表す。 */
sealed class ColumnSelectionResult {
    /** 自動判定できた場合。[valueColumnIndex] をそのまま値列として使用する。 */
    data class Resolved(val valueColumnIndex: Int) : ColumnSelectionResult()

    /** 自動判定に失敗した場合。ユーザーに [candidates] から選ばせる。 */
    data class NeedsUserSelection(val candidates: List<ColumnCandidate>) : ColumnSelectionResult()
}

/**
 * 列選択UIに表示する列候補。
 *
 * @param columnIndex   グリッド内の列インデックス（[OcrColumnSelector.rebuildItems] にそのまま渡す）
 * @param headerLabel   ヘッダー行のテキスト（取得できれば）。取得できなければ空文字列
 * @param previewValues その列の先頭数項目分の値（空でないものを最大3件）
 */
data class ColumnCandidate(
    val columnIndex: Int,
    val headerLabel: String,
    val previewValues: List<String>
)
