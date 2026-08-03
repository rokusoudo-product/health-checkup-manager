package com.rokusoudo.healthcheckup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * OcrParser のテスト（Issue #12: 座標付きセル（OcrCell）ベースの
 * レイアウト解析への変更に伴い、新インタフェースに合わせて更新）。
 *
 * - 単一セル1行のケース（旧方式のOCR1行が丸ごと1セルとして渡される場合）は
 *   従来の正規表現ベースの分離ロジックにフォールバックし、従来どおりの挙動を維持する。
 * - 複数セルが同一行に並ぶ多列テーブルのケースは、座標クラスタリングによる
 *   グリッド復元結果（buildGrid）と、それをOcrItemに変換した結果（parse）の両方を検証する。
 */
class OcrParserTest {

    /** テスト用のOcrCellを簡潔に生成するヘルパー。 */
    private fun cell(text: String, left: Int, top: Int, right: Int, bottom: Int, page: Int = 0) =
        OcrCell(text = text, left = left, top = top, right = right, bottom = bottom, page = page)

    // ------------------------------------------------------------
    // 後方互換: 単一セル1行（従来のOCR1行文字列相当）のケース
    // ------------------------------------------------------------

    @Test
    fun `単一セルの項目名+数値+単位を解析できる`() {
        val cells = listOf(cell("血圧(収縮期) 120 mmHg", 0, 0, 300, 40))
        val result = OcrParser.parse(cells)
        assertEquals(listOf(OcrItem("血圧(収縮期)", "120", "mmHg")), result)
    }

    @Test
    fun `小数値とパーセント単位を解析できる`() {
        val cells = listOf(cell("HbA1c 5.6 %", 0, 0, 200, 40))
        val result = OcrParser.parse(cells)
        assertEquals(listOf(OcrItem("HbA1c", "5.6", "%")), result)
    }

    @Test
    fun `スラッシュを含む単位を解析できる`() {
        val cells = listOf(cell("AST(GOT) 25 U/L", 0, 0, 200, 40))
        val result = OcrParser.parse(cells)
        assertEquals(listOf(OcrItem("AST(GOT)", "25", "U/L")), result)
    }

    @Test
    fun `単位なしの行は空単位で解析される`() {
        val cells = listOf(cell("体重 68.5", 0, 0, 150, 40))
        val result = OcrParser.parse(cells)
        assertEquals(listOf(OcrItem("体重", "68.5", "")), result)
    }

    @Test
    fun `複数行から数値を含む行のみ抽出しヘッダ行はスキップする`() {
        // 単一列の単純な健診表（従来動作していたケース）を、行ごとに独立したセルで再現する。
        val cells = listOf(
            cell("健康診断結果", 0, 0, 200, 40),
            cell("身長 172.5 cm", 0, 100, 250, 140),
            cell("体重 68.5 kg", 0, 200, 250, 240),
            cell("ご自愛ください", 0, 300, 200, 340)
        )
        val result = OcrParser.parse(cells)
        assertEquals(
            listOf(
                OcrItem("身長", "172.5", "cm"),
                OcrItem("体重", "68.5", "kg")
            ),
            result
        )
    }

    @Test
    fun `全行アンマッチの場合は空のOcrItem1件を返す`() {
        val cells = listOf(cell("読み取り不能なテキストのみ", 0, 0, 300, 40))
        val result = OcrParser.parse(cells)
        assertEquals(1, result.size)
        assertTrue(result[0].itemName.isBlank() && result[0].value.isBlank())
    }

    @Test
    fun `空リスト入力の場合は空のOcrItem1件を返す`() {
        val result = OcrParser.parse(emptyList())
        assertEquals(listOf(OcrItem("", "", "")), result)
    }

    // ------------------------------------------------------------
    // 多列の健診表（座標クラスタリングによるグリッド復元）
    // ------------------------------------------------------------

    @Test
    fun `多列の健診表で複数の数値が別々の列セルに分離される`() {
        // 「項目名 / 今回 / 前回 / 基準値」が横に並ぶ1行を、列ごとに独立したセルで表現する。
        val cells = listOf(
            cell("HbA1c", 0, 0, 100, 40),
            cell("5.6", 200, 0, 240, 40),
            cell("5.4", 320, 0, 360, 40),
            cell("4.6-6.2", 440, 0, 540, 40)
        )

        val grid = OcrParser.buildGrid(cells)
        // 1行4列に復元され、3つの数値がそれぞれ別の列として分離されていること
        // （1本の文字列に混ざって "5.6 5.4 4.6-6.2" のようにならないこと）を検証する。
        assertEquals(1, grid.size)
        assertEquals(listOf("HbA1c", "5.6", "5.4", "4.6-6.2"), grid[0])

        // 暫定ルール（1列目=項目名、2列目=値）で変換される
        val result = OcrParser.parse(cells)
        assertEquals(listOf(OcrItem("HbA1c", "5.6", "")), result)
    }

    @Test
    fun `buildGridは2行3列の表からグリッドを復元する`() {
        // ヘッダー行 + データ行の2行、「項目 / 今回 / 基準値」の3列
        val cells = listOf(
            cell("項目", 0, 0, 80, 40),
            cell("今回", 200, 0, 240, 40),
            cell("基準値", 400, 0, 460, 40),
            cell("体重", 0, 100, 80, 140),
            cell("68.5", 200, 100, 240, 140),
            cell("60-80", 400, 100, 470, 140)
        )

        val grid = OcrParser.buildGrid(cells)

        assertEquals(
            listOf(
                listOf("項目", "今回", "基準値"),
                listOf("体重", "68.5", "60-80")
            ),
            grid
        )
    }

    @Test
    fun `複数ページのセルはページごとに独立してクラスタリングされ結果は連結される`() {
        // 1枚目・2枚目は座標系が独立しているため、pageが異なれば座標が重なっていても
        // 別々にクラスタリングされ、結果は撮影順に連結される。
        val cells = listOf(
            cell("身長 172.5 cm", 0, 0, 250, 40, page = 0),
            cell("体重 68.5 kg", 0, 0, 250, 40, page = 1)
        )

        val result = OcrParser.parse(cells)

        assertEquals(
            listOf(
                OcrItem("身長", "172.5", "cm"),
                OcrItem("体重", "68.5", "kg")
            ),
            result
        )
    }
}
