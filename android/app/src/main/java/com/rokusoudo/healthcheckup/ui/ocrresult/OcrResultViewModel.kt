package com.rokusoudo.healthcheckup.ui.ocrresult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusoudo.healthcheckup.ItemMasterMatcher
import com.rokusoudo.healthcheckup.OcrItem
import com.rokusoudo.healthcheckup.data.repository.HealthRepository
import com.rokusoudo.healthcheckup.ui.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    // Issue #15: OCR抽出結果を項目マスタに照合した結果。未実行の間はnull。
    private val _masterMatchResult = MutableStateFlow<ItemMasterMatcher.MatchResult?>(null)
    val masterMatchResult: StateFlow<ItemMasterMatcher.MatchResult?> = _masterMatchResult

    /**
     * OCR抽出結果を項目マスタ（[HealthRepository.getAllMasters]）に照合する（Issue #15）。
     * 照合ロジック自体は [ItemMasterMatcher] に切り出しており、ここではマスタ取得と
     * 結果の反映のみを担う。
     */
    fun matchItemsAgainstMaster(items: List<OcrItem>) {
        viewModelScope.launch {
            val masters = repository.getAllMasters().first()
            _masterMatchResult.value = ItemMasterMatcher.match(items, masters)
        }
    }

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
