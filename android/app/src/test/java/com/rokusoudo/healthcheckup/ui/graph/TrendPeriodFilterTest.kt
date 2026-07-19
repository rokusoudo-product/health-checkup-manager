package com.rokusoudo.healthcheckup.ui.graph

import com.rokusoudo.healthcheckup.data.db.dao.ItemTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * S-04 グラフの期間切替（1年/全期間、決定Q3）と最新値表示ロジックのテスト（刷新001・Phase4）。
 */
class TrendPeriodFilterTest {

    private fun trend(date: String, value: String = "22.0") = ItemTrend(
        id = 0, recordId = 0, itemName = "BMI", value = value, unit = "kg/m2",
        referenceMin = null, referenceMax = null, isAbnormal = false, recordDate = date
    )

    private val today: LocalDate = LocalDate.of(2026, 7, 19)

    @Test
    fun `全期間はすべてのデータを返す`() {
        val trends = listOf(trend("2020-01-01"), trend("2025-06-01"), trend("2026-06-01"))
        assertEquals(3, TrendPeriodFilter.filter(trends, TrendPeriod.ALL, today).size)
    }

    @Test
    fun `1年は今日から1年以内のデータのみ返す`() {
        val trends = listOf(
            trend("2020-01-01"),
            trend("2025-07-18"),  // 1年+1日前 → 除外
            trend("2025-07-19"),  // ちょうど1年前 → 含む
            trend("2026-06-01")
        )
        val filtered = TrendPeriodFilter.filter(trends, TrendPeriod.ONE_YEAR, today)
        assertEquals(listOf("2025-07-19", "2026-06-01"), filtered.map { it.recordDate })
    }

    @Test
    fun `日付として解釈できない行は1年フィルタで除外される`() {
        val trends = listOf(trend("不正な日付"), trend("2026-06-01"))
        val filtered = TrendPeriodFilter.filter(trends, TrendPeriod.ONE_YEAR, today)
        assertEquals(listOf("2026-06-01"), filtered.map { it.recordDate })
    }

    @Test
    fun `最新値は記録日が最も新しい行を返す`() {
        val trends = listOf(trend("2024-06-01", "24.0"), trend("2026-06-01", "22.0"), trend("2025-06-01", "23.0"))
        assertEquals("22.0", TrendPeriodFilter.latest(trends)?.value)
    }

    @Test
    fun `データなしの場合の最新値はnull`() {
        assertNull(TrendPeriodFilter.latest(emptyList()))
    }
}
