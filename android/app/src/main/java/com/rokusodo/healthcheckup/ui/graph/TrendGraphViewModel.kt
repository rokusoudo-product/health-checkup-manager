package com.rokusodo.healthcheckup.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.data.db.dao.ItemTrend
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrendGraphViewModel(
    private val repository: HealthRepository,
    val itemName: String
) : ViewModel() {

    private val _trends = MutableStateFlow<List<ItemTrend>>(emptyList())
    val trends: StateFlow<List<ItemTrend>> = _trends

    init {
        loadTrends()
    }

    private fun loadTrends() {
        viewModelScope.launch {
            _trends.value = repository.getItemTrend(itemName)
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
