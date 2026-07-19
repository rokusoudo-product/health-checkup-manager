package com.rokusoudo.healthcheckup.ui.itemlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rokusoudo.healthcheckup.R
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import com.rokusoudo.healthcheckup.databinding.ItemItemListBinding
import com.rokusoudo.healthcheckup.ui.common.CategoryColors

/**
 * S-03 項目一覧のアダプタ。
 * カテゴリ色を枠線＋項目名テキストに適用し、♥お気に入りトグルを表示する。
 */
class ItemListAdapter(
    private val onItemClick: (ItemMaster) -> Unit,
    private val onFavoriteClick: (ItemMaster) -> Unit
) : ListAdapter<ItemMaster, ItemListAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(master: ItemMaster) {
            val context = binding.root.context
            val categoryColor = ContextCompat.getColor(context, CategoryColors.colorRes(master.category))

            binding.tvCategory.text = master.category
            binding.tvCategory.setTextColor(categoryColor)
            binding.tvItemName.text = master.itemName
            binding.tvItemName.setTextColor(categoryColor)
            binding.cardItem.strokeColor = categoryColor

            binding.tvUnit.text = master.unit

            if (master.isFavorite) {
                binding.btnFavorite.text = context.getString(R.string.favorite_on_mark)
                binding.btnFavorite.setTextColor(ContextCompat.getColor(context, R.color.favorite_on))
            } else {
                binding.btnFavorite.text = context.getString(R.string.favorite_off_mark)
                binding.btnFavorite.setTextColor(ContextCompat.getColor(context, R.color.favorite_off))
            }

            binding.cardItem.setOnClickListener { onItemClick(master) }
            binding.btnFavorite.setOnClickListener { onFavoriteClick(master) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ItemMaster>() {
            override fun areItemsTheSame(oldItem: ItemMaster, newItem: ItemMaster) =
                oldItem.itemName == newItem.itemName

            override fun areContentsTheSame(oldItem: ItemMaster, newItem: ItemMaster) =
                oldItem == newItem
        }
    }
}
