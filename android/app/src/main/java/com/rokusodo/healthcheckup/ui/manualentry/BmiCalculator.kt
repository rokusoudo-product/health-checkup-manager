package com.rokusodo.healthcheckup.ui.manualentry

import java.util.Locale

/**
 * S-06b BMI自動計算（決定Q8: 身長・体重から自動計算＋手動上書き可）。
 * 薬事法対応: 計算値の表示のみ。判定・助言は行わない。
 */
object BmiCalculator {

    /**
     * BMI = 体重kg / (身長m)^2 を小数1桁の文字列で返す。
     * 入力が数値でない・空・0以下の場合は null（自動計算しない）。
     */
    fun calculate(heightCm: String?, weightKg: String?): String? {
        val height = heightCm?.toDoubleOrNull() ?: return null
        val weight = weightKg?.toDoubleOrNull() ?: return null
        if (height <= 0 || weight <= 0) return null
        val heightM = height / 100.0
        val bmi = weight / (heightM * heightM)
        return String.format(Locale.US, "%.1f", bmi)
    }
}
