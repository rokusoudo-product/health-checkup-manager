package com.rokusoudo.healthcheckup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [OcrColumnSelector] のテスト（Issue #14）。
 *
 * 複数列の健診表（今回/前回/前々回 + 参考基準値）から「今回」列を特定するロジックを検証する。
 * - 検査日ヘッダー行から最も新しい日付の列を選ぶケース（列の並び順に依存しないことを含む）
 * - 参考基準値列を候補から除外するケース（範囲表記・キーワードの両方）
 * - 自動判定に失敗し、ユーザー選択が必要と判定されるケース
 * - 選択された列で項目リストを再構築する [OcrColumnSelector.rebuildItems]
 */
class OcrColumnSelectorTest {

    // ------------------------------------------------------------
    // 検査日による自動判定（列の並び順に依存しないこと）
    // ------------------------------------------------------------

    @Test
    fun `最新回が最左列の場合その列が今回列として選ばれる`() {
        val grid = listOf(
            listOf("", "2025/8/22", "2024/8/20", "4.6-6.2"),
            listOf("HbA1c", "5.6", "5.4", "4.6-6.2")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    @Test
    fun `最新回が最右列の場合その列が今回列として選ばれる`() {
        val grid = listOf(
            listOf("", "2024/8/20", "2025/8/22", "4.6-6.2"),
            listOf("HbA1c", "5.4", "5.6", "4.6-6.2")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertEquals(ColumnSelectionResult.Resolved(2), result)
    }

    @Test
    fun `和暦の検査日でも最新列を判定できる`() {
        // R7(2025)/8/22 のほうが R6(2024)/8/20 より新しい
        val grid = listOf(
            listOf("", "R7.8.22", "R6.8.20"),
            listOf("HbA1c", "5.6", "5.4")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    @Test
    fun `タイトル行があってもヘッダー行を正しく見つけて今回列を判定できる`() {
        // 表の上に「健康診断結果」のようなタイトル行がある実際のスキャンを想定。
        // ヘッダー行判定は行0固定ではなく、日付セルが2つ以上ある行を探して行う。
        val grid = listOf(
            listOf("健康診断結果", "", ""),
            listOf("", "2025/8/22", "2024/8/20"),
            listOf("HbA1c", "5.6", "5.4")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    // ------------------------------------------------------------
    // 参考基準値列の除外
    // ------------------------------------------------------------

    @Test
    fun `範囲表記の列は参考基準値列として除外される`() {
        val grid = listOf(
            listOf("", "2025/8/22", ""),
            listOf("体重", "68.5", "60-80")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        // 基準値列(2)が除外され、候補が1列(1)のみになるため自動判定できる
        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    @Test
    fun `以下未満などのキーワードを含む列は参考基準値列として除外される`() {
        val grid = listOf(
            listOf("", "2025/8/22", "基準値"),
            listOf("収縮期血圧", "120", "140以下")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    // ------------------------------------------------------------
    // 自動判定に失敗するケース
    // ------------------------------------------------------------

    @Test
    fun `検査日ヘッダーがなく候補列が複数残る場合はユーザー選択を要求する`() {
        val grid = listOf(
            listOf("HbA1c", "5.6", "5.4"),
            listOf("体重", "68.5", "70.2")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertTrue(result is ColumnSelectionResult.NeedsUserSelection)
        val candidates = (result as ColumnSelectionResult.NeedsUserSelection).candidates
        assertEquals(listOf(1, 2), candidates.map { it.columnIndex })
        assertEquals(listOf("5.6", "68.5"), candidates[0].previewValues)
        assertEquals(listOf("5.4", "70.2"), candidates[1].previewValues)
    }

    @Test
    fun `検査日が同一で判別できない場合はユーザー選択を要求する`() {
        val grid = listOf(
            listOf("", "2025/8/22", "2025/8/22"),
            listOf("HbA1c", "5.6", "5.4")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertTrue(result is ColumnSelectionResult.NeedsUserSelection)
    }

    @Test
    fun `ヘッダー行は特定できても一部の列だけ検査日が読み取れない場合はユーザー選択を要求する`() {
        // ヘッダー行判定自体は成立する（列1・列3の2セルが日付として読める）が、
        // 列2の日付が読み取れないため、日付での自動判定はできない
        // （読めた列だけで最新を選ぶと誤った列を「今回」と判定しかねないため）。
        val grid = listOf(
            listOf("", "2025/8/22", "読み取れない日付", "2024/8/20"),
            listOf("HbA1c", "5.6", "5.5", "5.4")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertTrue(result is ColumnSelectionResult.NeedsUserSelection)
        val candidates = (result as ColumnSelectionResult.NeedsUserSelection).candidates
        assertEquals(listOf(1, 2, 3), candidates.map { it.columnIndex })
    }

    // ------------------------------------------------------------
    // 単一列の健診表（列選択不要）
    // ------------------------------------------------------------

    @Test
    fun `単一の値列しかない場合は列選択なしでその列が採用される`() {
        val grid = listOf(
            listOf("身長", "172.5")
        )

        val result = OcrColumnSelector.selectValueColumn(grid)

        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    @Test
    fun `空のグリッドでも例外を投げずデフォルト列を返す`() {
        val result = OcrColumnSelector.selectValueColumn(emptyList())

        assertEquals(ColumnSelectionResult.Resolved(1), result)
    }

    // ------------------------------------------------------------
    // rebuildItems: 選択された列で項目リストを再構築する
    // ------------------------------------------------------------

    @Test
    fun `rebuildItemsは指定した列インデックスの値でOcrItemを再構築する`() {
        val grid = listOf(
            listOf("HbA1c", "5.6", "5.4"),
            listOf("体重", "68.5", "70.2")
        )

        val result = OcrColumnSelector.rebuildItems(grid, valueColumnIndex = 2)

        assertEquals(
            listOf(
                OcrItem("HbA1c", "5.4", ""),
                OcrItem("体重", "70.2", "")
            ),
            result
        )
    }

    @Test
    fun `rebuildItemsは単一セル1行の場合は従来の正規表現フォールバックを使う`() {
        val grid = listOf(
            listOf("血圧(収縮期) 120 mmHg"),
            listOf("HbA1c", "5.6", "5.4")
        )

        val result = OcrColumnSelector.rebuildItems(grid, valueColumnIndex = 2)

        assertEquals(
            listOf(
                OcrItem("血圧(収縮期)", "120", "mmHg"),
                OcrItem("HbA1c", "5.4", "")
            ),
            result
        )
    }

    @Test
    fun `rebuildItemsは全行アンマッチの場合は空のOcrItem1件を返す`() {
        val grid = listOf(
            listOf("読み取り不能なテキストのみ")
        )

        val result = OcrColumnSelector.rebuildItems(grid, valueColumnIndex = 1)

        assertEquals(1, result.size)
        assertTrue(result[0].itemName.isBlank() && result[0].value.isBlank())
    }
}
