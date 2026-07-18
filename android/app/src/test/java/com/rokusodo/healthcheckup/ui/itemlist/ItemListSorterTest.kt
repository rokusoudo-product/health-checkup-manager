package com.rokusodo.healthcheckup.ui.itemlist

import com.rokusodo.healthcheckup.data.db.entity.ItemCategories
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * S-03 項目一覧の並び順ロジックのテスト（画面遷移刷新001・Phase4）。
 * 仕様: お気に入りは上部固定・登録順（favoritedAt 昇順、決定Q2）。
 *       非お気に入りはカテゴリ順（ItemCategories.ORDERED）→ 項目名順。
 */
class ItemListSorterTest {

    private fun master(
        name: String,
        category: String = ItemCategories.OTHER,
        isFavorite: Boolean = false,
        favoritedAt: Long? = null
    ) = ItemMaster(name, "", null, null, category, isFavorite, favoritedAt)

    @Test
    fun `お気に入りが上部に登録順で固定される`() {
        val sorted = ItemListSorter.sort(
            listOf(
                master("身長", ItemCategories.BODY),
                master("γ-GTP", ItemCategories.LIVER, isFavorite = true, favoritedAt = 200L),
                master("BMI", ItemCategories.BODY, isFavorite = true, favoritedAt = 100L)
            )
        )
        assertEquals(listOf("BMI", "γ-GTP", "身長"), sorted.map { it.itemName })
    }

    @Test
    fun `非お気に入りはカテゴリ順で並ぶ`() {
        val sorted = ItemListSorter.sort(
            listOf(
                master("尿蛋白", ItemCategories.URINE),
                master("AST(GOT)", ItemCategories.LIVER),
                master("収縮期血圧", ItemCategories.BLOOD_PRESSURE),
                master("体重", ItemCategories.BODY)
            )
        )
        assertEquals(
            listOf("体重", "収縮期血圧", "AST(GOT)", "尿蛋白"),
            sorted.map { it.itemName }
        )
    }

    @Test
    fun `同一カテゴリ内は項目名順で並び未知カテゴリは末尾に回る`() {
        val sorted = ItemListSorter.sort(
            listOf(
                master("独自項目", "ユーザー定義カテゴリ"),
                master("身長", ItemCategories.BODY),
                master("BMI", ItemCategories.BODY)
            )
        )
        assertEquals(listOf("BMI", "身長", "独自項目"), sorted.map { it.itemName })
    }
}
