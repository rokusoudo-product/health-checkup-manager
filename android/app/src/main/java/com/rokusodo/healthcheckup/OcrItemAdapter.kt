package com.rokusodo.healthcheckup

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.databinding.ItemOcrResultBinding

/**
 * OCR結果一覧の RecyclerView アダプター。
 * 各行は「項目名・値・単位」の編集可能フォーム（EditText）で表示する。
 * TextWatcher で編集内容を items に即時反映するため、getItems() は常に最新の値を返す。
 */
class OcrItemAdapter(
    private val items: MutableList<OcrItem>
) : RecyclerView.Adapter<OcrItemAdapter.OcrItemViewHolder>() {

    inner class OcrItemViewHolder(
        private val binding: ItemOcrResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // TextWatcher をフィールドで保持して再バインド時に付け替える
        private var nameWatcher: TextWatcher? = null
        private var valueWatcher: TextWatcher? = null
        private var unitWatcher: TextWatcher? = null

        fun bind(item: OcrItem, position: Int) {
            // 古い TextWatcher を外してから値をセット（無限ループ防止）
            binding.etItemName.removeTextChangedListener(nameWatcher)
            binding.etValue.removeTextChangedListener(valueWatcher)
            binding.etUnit.removeTextChangedListener(unitWatcher)

            binding.etItemName.setText(item.itemName)
            binding.etValue.setText(item.value)
            binding.etUnit.setText(item.unit)

            // 編集内容を items に即時反映する TextWatcher を登録
            nameWatcher = simpleWatcher { items[position] = items[position].copy(itemName = it) }
            valueWatcher = simpleWatcher { items[position] = items[position].copy(value = it) }
            unitWatcher  = simpleWatcher { items[position] = items[position].copy(unit = it) }

            binding.etItemName.addTextChangedListener(nameWatcher)
            binding.etValue.addTextChangedListener(valueWatcher)
            binding.etUnit.addTextChangedListener(unitWatcher)
        }

        private fun simpleWatcher(onChange: (String) -> Unit) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) { onChange(s?.toString() ?: "") }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OcrItemViewHolder {
        val binding = ItemOcrResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OcrItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OcrItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    /** ⊕項目追加（刷新001 S-06a）: 空行を末尾に追加し、不足項目を手入力できるようにする。 */
    fun addItem() {
        items.add(OcrItem("", "", ""))
        notifyItemInserted(items.size - 1)
    }

    /** 編集済み内容を含む全アイテムを返す。TextWatcher により items は常に最新状態。 */
    fun getItems(): List<OcrItem> = items.toList()
}
