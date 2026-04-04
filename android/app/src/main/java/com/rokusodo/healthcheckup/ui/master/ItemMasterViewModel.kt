package com.rokusodo.healthcheckup.ui.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemMasterViewModel(private val repository: HealthRepository) : ViewModel() {

    val masters: StateFlow<List<ItemMaster>> = repository.getAllMasters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun upsertMaster(master: ItemMaster) {
        viewModelScope.launch {
            repository.upsertMaster(master)
        }
    }

    class Factory(private val repository: HealthRepository) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ItemMasterViewModel(repository) as T
        }
    }
}
