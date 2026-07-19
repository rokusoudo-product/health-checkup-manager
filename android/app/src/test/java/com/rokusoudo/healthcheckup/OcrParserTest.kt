package com.rokusoudo.healthcheckup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * OcrParser のリグレッションテスト（画面遷移刷新001・Phase1）。
 * 既存挙動を固定する。仕様: 全行アンマッチ・空入力時は空の OcrItem 1件を返し手動入力を促す。
 */
class OcrParserTest {

    @Test
    fun `項目名+数値+単位の行を解析できる`() {
        val result = OcrParser.parse("血圧(収縮期) 120 mmHg")
        assertEquals(listOf(OcrItem("血圧(収縮期)", "120", "mmHg")), result)
    }

    @Test
    fun `小数値とパーセント単位を解析できる`() {
        val result = OcrParser.parse("HbA1c 5.6 %")
        assertEquals(listOf(OcrItem("HbA1c", "5.6", "%")), result)
    }

    @Test
    fun `スラッシュを含む単位を解析できる`() {
        val result = OcrParser.parse("AST(GOT) 25 U/L")
        assertEquals(listOf(OcrItem("AST(GOT)", "25", "U/L")), result)
    }

    @Test
    fun `単位なしの行は空単位で解析される`() {
        val result = OcrParser.parse("体重 68.5")
        assertEquals(listOf(OcrItem("体重", "68.5", "")), result)
    }

    @Test
    fun `複数行から数値を含む行のみ抽出しヘッダ行や空行はスキップする`() {
        val raw = """
            健康診断結果
            身長 172.5 cm

            体重 68.5 kg
            ご自愛ください
        """.trimIndent()
        val result = OcrParser.parse(raw)
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
        val result = OcrParser.parse("読み取り不能なテキストのみ")
        assertEquals(1, result.size)
        assertTrue(result[0].itemName.isBlank() && result[0].value.isBlank())
    }

    @Test
    fun `空文字入力の場合は空のOcrItem1件を返す`() {
        val result = OcrParser.parse("")
        assertEquals(listOf(OcrItem("", "", "")), result)
    }
}
