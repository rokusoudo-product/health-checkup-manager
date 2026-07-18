package com.rokusodo.healthcheckup.ui.itemlist

import com.rokusodo.healthcheckup.data.db.entity.ItemCategories
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster

/**
 * S-03 項目一覧の並び順ロジック。
 * お気に入りを上部固定・登録順（favoritedAt 昇順、決定Q2）で並べ、
 * 非お気に入りはカテゴリ順（ItemCategories.ORDERED）→ 項目名順で並べる。
 */
object ItemListSorter {

    fun sort(masters: List<ItemMaster>): List<ItemMaster> {
        val (favorites, others) = masters.partition { it.isFavorite }
        return favorites.sortedBy { it.favoritedAt ?: Long.MAX_VALUE } +
            others.sortedWith(
                compareBy({ ItemCategories.order(it.category) }, { it.itemName })
            )
    }
}
