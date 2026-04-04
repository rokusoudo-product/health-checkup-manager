package com.rokusodo.healthcheckup.ui.ocrresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.OcrItem
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 保存処理の状態 */
sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    data class Success(val recordId: Long) : SaveState()
    data class Error(val message: String) : SaveState()
}

class OcrResultViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun saveRecord(date: String, facility: String, items: List<OcrItem>) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                val recordId = repository.saveRecord(date, facility, items)
                _saveState.value = SaveState.Success(recordId)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "保存に失敗しました")
            }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }

    class Factory(private val repository: HealthRepository) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return OcrResultViewModel(repository) as T
        }
    }
}
