package com.rokusodo.healthcheckup.ui.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem
import com.rokusodo.healthcheckup.databinding.ItemExaminationItemBinding

/**
 * 検査項目一覧の RecyclerView アダプター。
 * 薬事法対応: isAbnormal はハイライト表示のみに使用し、医療判断を含まない。
 */
class RecordItemAdapter(
    private val onGraphClick: (itemName: String) -> Unit = {}
) : ListAdapter<ExaminationItem, RecordItemAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemExaminationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExaminationItem) {
            binding.tvItemName.text = item.itemName
            binding.tvItemValue.text = item.value
            binding.tvItemUnit.text = item.unit

            val refText = buildString {
                if (item.referenceMin != null || item.referenceMax != null) {
                    append("基準: ")
                    append(item.referenceMin?.toString() ?: "")
                    append(" - ")
                    append(item.referenceMax?.toString() ?: "")
                    if (item.unit.isNotBlank()) append(" ${item.unit}")
                }
            }
            binding.tvItemReference.text = refText

            // 薬事法対応: isAbnormal は表示ハイライトのみ
            if (item.isAbnormal) {
                binding.tvItemName.setTextColor(Color.RED)
                binding.tvItemValue.setTextColor(Color.RED)
            } else {
                val typedArray = binding.tvItemName.context.theme.obtainStyledAttributes(
                    intArrayOf(android.R.attr.textColorPrimary)
                )
                val defaultColor = typedArray.getColor(0, Color.BLACK)
                typedArray.recycle()
                binding.tvItemName.setTextColor(defaultColor)
                binding.tvItemValue.setTextColor(defaultColor)
            }

            // グラフボタン
            binding.btnGraph.setOnClickListener {
                onGraphClick(item.itemName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExaminationItemBinding.inflate(
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
