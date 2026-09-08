package com.rokusoudo.healthcheckup.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusoudo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordDetailViewModel(
    private val repository: HealthRepository,
    private val recordId: Long
) : ViewModel() {

    val items: StateFlow<List<ExaminationItem>> = repository.getItemsForRecord(recordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    /**
     * Issue #47: 確認ダイアログで「削除する」が選ばれた後に呼ばれる。
     * 二重タップでの多重削除を避けるため、進行中は再入しない。
     */
    fun deleteRecord() {
        if (_deleteState.value is DeleteState.InProgress) return
        _deleteState.value = DeleteState.InProgress
        viewModelScope.launch {
            val result = repository.deleteRecord(recordId)
            _deleteState.value = result.fold(
                onSuccess = { DeleteState.Success },
                onFailure = { DeleteState.Error(it.message) }
            )
        }
    }

    /** エラー表示（Toast等）を出し終えた後、状態をIdleへ戻す。 */
    fun consumeErrorState() {
        _deleteState.value = DeleteState.Idle
    }

    sealed class DeleteState {
        object Idle : DeleteState()
        object InProgress : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String?) : DeleteState()
    }

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
