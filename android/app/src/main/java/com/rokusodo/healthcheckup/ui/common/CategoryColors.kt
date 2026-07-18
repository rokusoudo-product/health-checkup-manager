package com.rokusodo.healthcheckup.ui.common

import androidx.annotation.ColorRes
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.data.db.entity.ItemCategories

/**
 * カテゴリ名 → カラートークン（colors.xml）の対応。
 * DESIGN.md: カテゴリ色は項目行の枠線＋項目名テキストに適用。背景には使わない。
 */
object CategoryColors {

    @ColorRes
    fun colorRes(category: String): Int = when (category) {
        ItemCategories.BODY -> R.color.category_body
        ItemCategories.BLOOD_PRESSURE -> R.color.category_blood_pressure
        ItemCategories.BLOOD -> R.color.category_blood
        ItemCategories.LIPID -> R.color.category_lipid
        ItemCategories.LIVER -> R.color.category_liver
        ItemCategories.KIDNEY -> R.color.category_kidney
        ItemCategories.GLUCOSE -> R.color.category_glucose
        ItemCategories.URINE -> R.color.category_urine
        else -> R.color.category_other
    }
}
