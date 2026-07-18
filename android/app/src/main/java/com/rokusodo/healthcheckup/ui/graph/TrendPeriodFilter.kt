package com.rokusodo.healthcheckup.ui.graph

import com.rokusodo.healthcheckup.data.db.dao.ItemTrend
import java.time.LocalDate

/** S-04 グラフの期間切替（決定Q3: 1年/全期間の2タブ） */
enum class TrendPeriod { ONE_YEAR, ALL }

/**
 * S-04 グラフの期間フィルタ・最新値ロジック。
 */
object TrendPeriodFilter {

    /** 指定期間内のトレンドを返す。recordDate が "yyyy-MM-dd" として解釈できない行は期間指定時に除外する。 */
    fun filter(
        trends: List<ItemTrend>,
        period: TrendPeriod,
        today: LocalDate = LocalDate.now()
    ): List<ItemTrend> = when (period) {
        TrendPeriod.ALL -> trends
        TrendPeriod.ONE_YEAR -> {
            val cutoff = today.minusYears(1)
            trends.filter { trend ->
                val date = runCatching { LocalDate.parse(trend.recordDate) }.getOrNull()
                date != null && !date.isBefore(cutoff)
            }
        }
    }

    /** 記録日が最も新しい行（最新値）。日付は "yyyy-MM-dd" のため文字列比較で最大を取る。 */
    fun latest(trends: List<ItemTrend>): ItemTrend? = trends.maxByOrNull { it.recordDate }
}
