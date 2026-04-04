package com.rokusodo.healthcheckup.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(repository: HealthRepository) : ViewModel() {

    val records: StateFlow<List<ExaminationRecord>> = repository.getAllRecords()
        .stateIn(
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
