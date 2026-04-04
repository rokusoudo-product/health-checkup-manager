package com.rokusodo.healthcheckup.ui.master

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster
import com.rokusodo.healthcheckup.databinding.ItemMasterBinding

/**
 * 項目マスター一覧の RecyclerView アダプター。
 */
class ItemMasterAdapter(
    private val onEditClick: (ItemMaster) -> Unit
) : ListAdapter<ItemMaster, ItemMasterAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMasterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(master: ItemMaster) {
            binding.tvMasterItemName.text = master.itemName
            binding.tvMasterUnit.text = master.unit.ifBlank { "-" }
            binding.tvMasterRefMin.text = master.referenceMin?.toString() ?: "-"
            binding.tvMasterRefMax.text = master.referenceMax?.toString() ?: "-"
            binding.btnMasterEdit.setOnClickListener { onEditClick(master) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMasterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemMaster>() {
        override fun areItemsTheSame(oldItem: ItemMaster, newItem: ItemMaster): Boolean =
            oldItem.itemName == newItem.itemName

        override fun areContentsTheSame(oldItem: ItemMaster, newItem: ItemMaster): Boolean =
            oldItem == newItem
    }
}
