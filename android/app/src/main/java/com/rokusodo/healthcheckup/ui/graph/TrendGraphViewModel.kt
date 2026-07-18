package com.rokusodo.healthcheckup.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.data.db.dao.ItemTrend
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * S-04 経年グラフの ViewModel。
 * 期間切替（1年/全期間、決定Q3）と最新値の表示状態を管理する。
 */
class TrendGraphViewModel(
    private val repository: HealthRepository,
    val itemName: String
) : ViewModel() {

    private val _allTrends = MutableStateFlow<List<ItemTrend>>(emptyList())

    private val _period = MutableStateFlow(TrendPeriod.ALL)
    val period: StateFlow<TrendPeriod> = _period

    /** 選択中の期間でフィルタしたトレンド（グラフ描画用） */
    val trends: StateFlow<List<ItemTrend>> =
        combine(_allTrends, _period) { trends, period ->
            TrendPeriodFilter.filter(trends, period)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 最新値（期間によらず全データの最新） */
    val latest: StateFlow<ItemTrend?> = _allTrends
        .map { TrendPeriodFilter.latest(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadTrends()
    }

    fun setPeriod(period: TrendPeriod) {
        _period.value = period
    }

    private fun loadTrends() {
        viewModelScope.launch {
            _allTrends.value = repository.getItemTrend(itemName)
        }
    }

    class Factory(
        private val repository: HealthRepository,
        private val itemName: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TrendGraphViewModel(repository, itemName) as T
        }
    }
}
