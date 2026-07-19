package com.rokusoudo.healthcheckup.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 項目マスター（基準値マスター）エンティティ。
 * 検査項目ごとの参考基準値・カテゴリ・お気に入り状態を管理する。
 * 薬事法対応: この値は医学的診断に使用しない。表示・ハイライトのみ。
 */
@Entity(tableName = "item_masters")
data class ItemMaster(
    @PrimaryKey val itemName: String,
    val unit: String,
    val referenceMin: Double?,
    val referenceMax: Double?,
    @ColumnInfo(defaultValue = "その他")
    val category: String = ItemCategories.OTHER,
    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean = false,
    /** お気に入り登録日時（epoch millis）。並び順=登録順（昇順）に使用。OFF時は null */
    val favoritedAt: Long? = null
)

/**
 * 検査項目カテゴリ。S-03 項目一覧・S-06b 手入力でカテゴリごとに色分け表示する。
 * 色トークンは DESIGN.md → res/values/colors.xml に定義（直書き禁止）。
 */
object ItemCategories {
    const val BODY = "身体計測"
    const val BLOOD_PRESSURE = "血圧"
    const val BLOOD = "血液一般"
    const val LIPID = "脂質"
    const val LIVER = "肝機能"
    const val KIDNEY = "腎機能"
    const val GLUCOSE = "糖代謝"
    const val URINE = "尿検査"
    const val OTHER = "その他"

    /** 画面表示のカテゴリ順 */
    val ORDERED = listOf(BODY, BLOOD_PRESSURE, BLOOD, LIPID, LIVER, KIDNEY, GLUCOSE, URINE, OTHER)

    /** 未知カテゴリを末尾に回すためのソートキー */
    fun order(category: String): Int = ORDERED.indexOf(category).let { if (it < 0) ORDERED.size else it }
}
