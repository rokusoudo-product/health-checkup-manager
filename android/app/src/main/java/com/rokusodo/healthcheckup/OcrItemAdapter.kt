package com.rokusodo.healthcheckup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rokusodo.healthcheckup.databinding.ItemOcrResultBinding

/**
 * OCR結果一覧の RecyclerView アダプター。
 * 各行は「項目名・値・単位」の編集可能フォーム（EditText）で表示する。
 */
class OcrItemAdapter(
    private val items: MutableList<OcrItem>
) : RecyclerView.Adapter<OcrItemAdapter.OcrItemViewHolder>() {

    inner class OcrItemViewHolder(
        private val binding: ItemOcrResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OcrItem) {
            // EditTextに値をセット（既存テキストをリセットしてから）
            binding.etItemName.setText(item.itemName)
            binding.etValue.setText(item.value)
            binding.etUnit.setText(item.unit)
        }

        /**
         * ViewHolder が保持している現在の編集内容を OcrItem として返す。
         */
        fun getCurrentItem(): OcrItem = OcrItem(
            itemName = binding.etItemName.text?.toString() ?: "",
            value = binding.etValue.text?.toString() ?: "",
            unit = binding.etUnit.text?.toString() ?: ""
        )
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
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /**
     * 現在の RecyclerView の全アイテムを（編集済み内容を含めて）返す。
     * RecyclerView の ViewHolder がリサイクルされている可能性を考慮し、
     * バインドされた ViewHolder からのみ取得し、それ以外は元のデータを使用する。
     *
     * 注意: Sprint 2 で保存機能を実装する際は DiffUtil + ViewModel + Flow を使用すること。
     */
    fun getItems(): List<OcrItem> = items.toList()
}
