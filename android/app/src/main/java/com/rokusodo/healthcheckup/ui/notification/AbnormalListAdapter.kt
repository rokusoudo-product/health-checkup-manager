package com.rokusodo.healthcheckup.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusodo.healthcheckup.databinding.ItemAbnormalBinding

/**
 * 基準値外項目一覧の RecyclerView アダプター。
 * 薬事法対応: 「要注意」「危険」などの医療判断テキストは含まない。
 */
class AbnormalListAdapter : ListAdapter<ExaminationItem, AbnormalListAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemAbnormalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExaminationItem) {
            binding.tvAbnormalItemName.text = item.itemName
            binding.tvAbnormalValue.text = "${item.value} ${item.unit}".trim()

            val refText = buildString {
                if (item.referenceMin != null || item.referenceMax != null) {
                    append("基準: ")
                    append(item.referenceMin?.toString() ?: "")
                    append(" - ")
                    append(item.referenceMax?.toString() ?: "")
                    if (item.unit.isNotBlank()) append(" ${item.unit}")
                }
            }
            binding.tvAbnormalReference.text = refText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAbnormalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ExaminationItem>() {
        override fun areItemsTheSame(oldItem: ExaminationItem, newItem: ExaminationItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ExaminationItem, newItem: ExaminationItem): Boolean =
            oldItem == newItem
    }
}
