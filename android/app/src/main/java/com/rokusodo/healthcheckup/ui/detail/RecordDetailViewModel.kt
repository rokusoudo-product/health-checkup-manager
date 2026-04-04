package com.rokusodo.healthcheckup.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecordDetailViewModel(
    repository: HealthRepository,
    recordId: Long
) : ViewModel() {

    val items: StateFlow<List<ExaminationItem>> = repository.getItemsForRecord(recordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    class Factory(
        private val repository: HealthRepository,
        private val recordId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RecordDetailViewModel(repository, recordId) as T
        }
    }
}
