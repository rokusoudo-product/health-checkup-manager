package com.rokusodo.healthcheckup.ui.ocrresult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusodo.healthcheckup.OcrItem
import com.rokusodo.healthcheckup.data.repository.HealthRepository
import com.rokusodo.healthcheckup.ui.notification.NotificationHelper
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

class OcrResultViewModel(
    application: Application,
    private val repository: HealthRepository
) : AndroidViewModel(application) {

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun saveRecord(date: String, facility: String, items: List<OcrItem>) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                val recordId = repository.saveRecord(date, facility, items)

                // 保存完了後、基準値外項目があれば通知を送る（薬事法対応: 事実のみ通知）
                val abnormalItems = repository.getItemsForRecordSnapshot(recordId)
                    .filter { it.isAbnormal }
                if (abnormalItems.isNotEmpty()) {
                    NotificationHelper.sendAbnormalAlert(
                        context = getApplication(),
                        recordId = recordId,
                        abnormalItems = abnormalItems
                    )
                }

                _saveState.value = SaveState.Success(recordId)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "保存に失敗しました")
            }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }

    class Factory(
        private val application: Application,
        private val repository: HealthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return OcrResultViewModel(application, repository) as T
        }
    }
}
