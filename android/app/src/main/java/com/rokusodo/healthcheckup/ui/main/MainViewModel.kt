package com.rokusodo.healthcheckup.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(repository: HealthRepository) : ViewModel() {

    /**
     * 診断記録と、記録ごとの基準値外項目数を結合して公開する。
     * 一覧の状態バッジ（要注意 N件 / 異常なし）に使用する。
     */
    val records: StateFlow<List<RecordListAdapter.RecordWithAbnormalCount>> =
        combine(
            repository.getAllRecords(),
            repository.getAbnormalCountsByRecord()
        ) { records, counts ->
            val countMap = counts.associate { it.recordId to it.count }
            records.map { record ->
                RecordListAdapter.RecordWithAbnormalCount(
                    record = record,
                    abnormalCount = countMap[record.id] ?: 0
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    class Factory(private val repository: HealthRepository) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
    }
}
