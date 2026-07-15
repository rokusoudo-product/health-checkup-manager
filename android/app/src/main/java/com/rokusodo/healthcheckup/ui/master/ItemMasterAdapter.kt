package com.rokusodo.healthcheckup.ui.master

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.R
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
            binding.tvMasterReference.text = formatReference(master)
            binding.ivMasterIcon.setImageResource(iconFor(master.itemName))
            binding.btnMasterEdit.setOnClickListener { onEditClick(master) }
        }

        /**
         * 基準値を統一フォーマットで表示する。
         * 上限のみ: 「〜30.0」 / 下限のみ: 「40.0〜」 / 範囲: 「18.5〜25.0」。
         * 単位があれば末尾に付与する。
         */
        private fun formatReference(master: ItemMaster): String {
            val min = master.referenceMin
            val max = master.referenceMax
            val range = when {
                min != null && max != null -> "${fmt(min)}〜${fmt(max)}"
                min != null -> "${fmt(min)}〜"
                max != null -> "〜${fmt(max)}"
                else -> "未設定"
            }
            val unit = master.unit.trim()
            return if (unit.isNotEmpty() && (min != null || max != null)) {
                "基準値 $range $unit"
            } else if (min != null || max != null) {
                "基準値 $range"
            } else {
                range
            }
        }

        /** 末尾の .0 を落として数値を表示する（129.0 → 129, 18.5 → 18.5）。 */
        private fun fmt(value: Double): String =
            if (value == value.toLong().toDouble()) value.toLong().toString()
            else value.toString()

        /** 項目名からカテゴリを推定してアイコンを割り当てる。 */
        private fun iconFor(itemName: String): Int = when {
            itemName.contains("血圧") -> R.drawable.ic_cat_bp
            itemName.contains("BMI") || itemName.contains("体重") ||
                itemName.contains("身長") || itemName.contains("腹囲") -> R.drawable.ic_cat_body
            itemName.contains("AST") || itemName.contains("ALT") ||
                itemName.contains("GTP") || itemName.contains("血糖") ||
                itemName.contains("コレステロール") || itemName.contains("中性脂肪") ||
                itemName.contains("HDL") || itemName.contains("LDL") -> R.drawable.ic_cat_blood
            else -> R.drawable.ic_cat_default
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
