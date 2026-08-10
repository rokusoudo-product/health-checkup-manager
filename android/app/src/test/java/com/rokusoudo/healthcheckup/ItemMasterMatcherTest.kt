package com.rokusoudo.healthcheckup

import com.rokusoudo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusoudo.healthcheckup.data.db.entity.ItemCategories
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ItemMasterMatcher のテスト（Issue #15）。
 *
 * OCRで抽出した項目名を項目マスタに正規化・照合し、該当しない行を除外するロジックを検証する。
 * - 全角/半角・カッコ種別・スペース有無の表記ゆれ吸収
 * - 編集距離ベースのあいまい一致（γ-GTP/γ-GT のような別名・脱字）
 * - あいまい一致の閾値を満たさない行（OCR誤認識由来のゴミ）の除外
 * - 単位のマスタ値への統一
 * - マスタに存在する項目が誤って除外されないこと（回帰）
 */
class ItemMasterMatcherTest {

    private val masters = listOf(
        ItemMaster("身長", "cm", null, null, ItemCategories.BODY),
        ItemMaster("体重", "kg", null, null, ItemCategories.BODY),
        ItemMaster("BMI", "kg/m2", 18.5, 25.0, ItemCategories.BODY),
        ItemMaster("AST(GOT)", "U/L", null, 30.0, ItemCategories.LIVER),
        ItemMaster("γ-GTP", "U/L", null, 50.0, ItemCategories.LIVER),
        ItemMaster("HbA1c", "%", null, 5.5, ItemCategories.GLUCOSE)
    )

    private fun item(name: String, value: String, unit: String = "") = OcrItem(name, value, unit)

    // ------------------------------------------------------------
    // 完全一致・表記ゆれ吸収
    // ------------------------------------------------------------

    @Test
    fun `完全一致する項目名はそのまま採用される`() {
        val result = ItemMasterMatcher.match(listOf(item("身長", "172.5", "cm")), masters)
        assertEquals(0, result.excludedCount)
        assertEquals(listOf(OcrItem("身長", "172.5", "cm")), result.items)
    }

    @Test
    fun `全角英数字とスペースの表記ゆれが吸収されてマスタ名に正規化される`() {
        // OCR誤認識により全角化・スペース混入した「ＡＳＴ （ ＧＯＴ ）」を想定
        val result = ItemMasterMatcher.match(listOf(item("ＡＳＴ （ ＧＯＴ ）", "25", "U/L")), masters)
        assertEquals(0, result.excludedCount)
        assertEquals("AST(GOT)", result.items.single().itemName)
    }

    @Test
    fun `カッコ種別の違いが吸収されてマスタ名に正規化される`() {
        val bracketVariants = listOf("AST[GOT]", "AST【GOT】", "AST〔GOT〕", "AST（GOT）")
        for (variant in bracketVariants) {
            val result = ItemMasterMatcher.match(listOf(item(variant, "25", "U/L")), masters)
            assertEquals("variant=$variant", 0, result.excludedCount)
            assertEquals("variant=$variant", "AST(GOT)", result.items.single().itemName)
        }
    }

    @Test
    fun `OCR側の単位表記に関わらずマスタの単位が採用される`() {
        val result = ItemMasterMatcher.match(listOf(item("体重", "68.5", "ｋｇ")), masters)
        assertEquals(0, result.excludedCount)
        // 単位はマスタ値優先のため OCR 側の全角単位はそのまま使われない
        assertEquals("kg", result.items.single().unit)
    }

    // ------------------------------------------------------------
    // あいまい一致（編集距離）
    // ------------------------------------------------------------

    @Test
    fun `1文字脱落したγ-GTPの別名がγ-GTPに正規化される`() {
        val result = ItemMasterMatcher.match(listOf(item("γ-GT", "45", "U/L")), masters)
        assertEquals(0, result.excludedCount)
        assertEquals("γ-GTP", result.items.single().itemName)
    }

    @Test
    fun `OCR誤認識による1文字違いはあいまい一致でマスタ名に正規化される`() {
        val result = ItemMasterMatcher.match(listOf(item("HbAlc", "5.6", "%")), masters)
        assertEquals(0, result.excludedCount)
        assertEquals("HbA1c", result.items.single().itemName)
    }

    // ------------------------------------------------------------
    // 閾値未満（ゴミ行）の除外
    // ------------------------------------------------------------

    @Test
    fun `罫線由来の意味をなさない項目名22-7は除外される`() {
        val result = ItemMasterMatcher.match(listOf(item("22/7", "23", "/")), masters)
        assertEquals(1, result.excludedCount)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `注記由来の意味をなさない項目名87-0は除外される`() {
        val result = ItemMasterMatcher.match(listOf(item("87.0", "1", "")), masters)
        assertEquals(1, result.excludedCount)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `該当行と除外行が混在する場合は該当行のみ採用され件数が正しくカウントされる`() {
        val result = ItemMasterMatcher.match(
            listOf(
                item("身長", "172.5", "cm"),
                item("22/7", "23", "/"),
                item("体重", "68.5", "kg"),
                item("87.0", "1", "")
            ),
            masters
        )
        assertEquals(2, result.excludedCount)
        assertEquals(
            listOf(OcrItem("身長", "172.5", "cm"), OcrItem("体重", "68.5", "kg")),
            result.items
        )
    }

    // ------------------------------------------------------------
    // 単位のマスタ値への統一
    // ------------------------------------------------------------

    @Test
    fun `OCRで読み取った単位がマスタと異なる場合マスタの単位が採用される`() {
        val result = ItemMasterMatcher.match(listOf(item("BMI", "22.0", "kg/m^2")), masters)
        assertEquals("kg/m2", result.items.single().unit)
    }

    // ------------------------------------------------------------
    // 空行（手動入力用プレースホルダー）は照合対象外
    // ------------------------------------------------------------

    @Test
    fun `項目名も値も空欄の行は照合対象外としてそのまま保持される`() {
        val result = ItemMasterMatcher.match(listOf(item("", "", "")), masters)
        assertEquals(0, result.excludedCount)
        assertEquals(listOf(OcrItem("", "", "")), result.items)
    }

    // ------------------------------------------------------------
    // マスタが空の場合
    // ------------------------------------------------------------

    @Test
    fun `マスタが空の場合は実データ行が全て除外される`() {
        val result = ItemMasterMatcher.match(listOf(item("身長", "172.5", "cm")), emptyList())
        assertEquals(1, result.excludedCount)
        assertTrue(result.items.isEmpty())
    }

    // ------------------------------------------------------------
    // 回帰: マスタに存在する項目が誤って除外されないこと
    // ------------------------------------------------------------

    @Test
    fun `デフォルト項目マスタの全項目は自分自身に完全一致し誤って除外されない`() {
        val defaultMasters = HealthCheckupDatabase.DEFAULT_ITEM_MASTERS
        for (master in defaultMasters) {
            val result = ItemMasterMatcher.match(
                listOf(item(master.itemName, "1", master.unit)),
                defaultMasters
            )
            assertEquals("item=${master.itemName}", 0, result.excludedCount)
            assertEquals("item=${master.itemName}", master.itemName, result.items.single().itemName)
        }
    }

    // ------------------------------------------------------------
    // 正規化・類似度の単体テスト
    // ------------------------------------------------------------

    @Test
    fun `normalizeは全角半角・カッコ・スペース・大文字小文字の差異を吸収する`() {
        assertEquals(ItemMasterMatcher.normalize("AST(GOT)"), ItemMasterMatcher.normalize("ａｓｔ （ｇｏｔ）"))
        assertEquals(ItemMasterMatcher.normalize("AST(GOT)"), ItemMasterMatcher.normalize("AST[GOT]"))
    }

    @Test
    fun `similarityは完全一致で1-0を返し無関係な文字列では低い値を返す`() {
        assertEquals(1.0, ItemMasterMatcher.similarity("abc", "abc"), 0.0001)
        assertTrue(ItemMasterMatcher.similarity("22/7", "身長") < 0.5)
    }
}
