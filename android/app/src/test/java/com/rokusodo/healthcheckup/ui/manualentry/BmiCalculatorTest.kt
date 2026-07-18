package com.rokusodo.healthcheckup.ui.manualentry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * S-06b BMI自動計算のテスト（刷新001・Phase5・決定Q8）。
 * BMI = 体重kg / (身長m)^2。小数1桁の文字列で返す。
 */
class BmiCalculatorTest {

    @Test
    fun `身長と体重からBMIを計算できる`() {
        // 68.5 / 1.70^2 = 23.70... → 23.7
        assertEquals("23.7", BmiCalculator.calculate("170", "68.5"))
    }

    @Test
    fun `小数の身長でも計算できる`() {
        // 55.0 / 1.625^2 = 20.82... → 20.8
        assertEquals("20.8", BmiCalculator.calculate("162.5", "55"))
    }

    @Test
    fun `身長か体重が空なら計算しない`() {
        assertNull(BmiCalculator.calculate("", "68.5"))
        assertNull(BmiCalculator.calculate("170", ""))
        assertNull(BmiCalculator.calculate(null, null))
    }

    @Test
    fun `数値でない入力は計算しない`() {
        assertNull(BmiCalculator.calculate("abc", "68.5"))
        assertNull(BmiCalculator.calculate("170", "－"))
    }

    @Test
    fun `ゼロ以下の入力は計算しない`() {
        assertNull(BmiCalculator.calculate("0", "68.5"))
        assertNull(BmiCalculator.calculate("170", "-1"))
    }
}
