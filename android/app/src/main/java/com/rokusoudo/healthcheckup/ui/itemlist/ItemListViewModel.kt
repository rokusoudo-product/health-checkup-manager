package com.rokusoudo.healthcheckup.ui.itemlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import com.rokusoudo.healthcheckup.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * S-03 項目一覧の ViewModel。
 * 並び順は ItemListSorter（お気に入り上部固定・登録順、決定Q2）。
 */
class ItemListViewModel(
    private val repository: HealthRepository
) : ViewModel() {

    val items: StateFlow<List<ItemMaster>> = repository.getAllMasters()
        .map { ItemListSorter.sort(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** ♥トグル。ON時は favoritedAt に現在時刻を記録（登録順の並びに使用） */
    fun toggleFavorite(master: ItemMaster) {
        viewModelScope.launch {
            val updated = if (master.isFavorite) {
                master.copy(isFavorite = false, favoritedAt = null)
            } else {
                master.copy(isFavorite = true, favoritedAt = System.currentTimeMillis())
            }
            repository.upsertMaster(updated)
        }
    }

    class Factory(private val repository: HealthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ItemListViewModel(repository) as T
        }
    }
}
