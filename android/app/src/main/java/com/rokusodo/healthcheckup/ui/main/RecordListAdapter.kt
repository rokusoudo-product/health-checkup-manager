package com.rokusodo.healthcheckup.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord
import com.rokusodo.healthcheckup.databinding.ItemRecordBinding

/**
 * 診断記録一覧の RecyclerView アダプター。
 * DiffUtil.ItemCallback を使用してリスト差分更新を行う。
 */
class RecordListAdapter(
    private val onItemClick: (ExaminationRecord) -> Unit
) : ListAdapter<RecordListAdapter.RecordWithAbnormalCount, RecordListAdapter.ViewHolder>(DiffCallback()) {

    data class RecordWithAbnormalCount(
        val record: ExaminationRecord,
        val abnormalCount: Int
    )

    inner class ViewHolder(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordWithAbnormalCount) {
            binding.tvRecordDate.text = item.record.date
            binding.tvRecordFacility.text = item.record.facility.ifBlank { "-" }
            if (item.abnormalCount > 0) {
                binding.tvAbnormalBadge.text = item.abnormalCount.toString()
                binding.tvAbnormalBadge.visibility = android.view.View.VISIBLE
            } else {
                binding.tvAbnormalBadge.visibility = android.view.View.GONE
            }
            binding.root.setOnClickListener { onItemClick(item.record) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<RecordWithAbnormalCount>() {
        override fun areItemsTheSame(
            oldItem: RecordWithAbnormalCount,
            newItem: RecordWithAbnormalCount
        ): Boolean = oldItem.record.id == newItem.record.id

        override fun areContentsTheSame(
            oldItem: RecordWithAbnormalCount,
            newItem: RecordWithAbnormalCount
        ): Boolean = oldItem == newItem
    }
}
