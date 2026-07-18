package com.rokusodo.healthcheckup

import android.app.Application
import android.view.ContextThemeWrapper
import android.widget.EditText
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * S-06a OCR候補の確認・補正フローのテスト（刷新001・Phase5）。
 * OCR候補の編集反映と ⊕項目追加を検証する。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class OcrItemAdapterTest {

    private fun createHolder(adapter: OcrItemAdapter): OcrItemAdapter.OcrItemViewHolder {
        val context = ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.Theme_HealthCheckupManager
        )
        return adapter.onCreateViewHolder(FrameLayout(context), 0)
    }

    @Test
    fun `項目追加で空行が末尾に追加される`() {
        val adapter = OcrItemAdapter(mutableListOf(OcrItem("体重", "68.5", "kg")))
        adapter.addItem()
        assertEquals(2, adapter.itemCount)
        assertEquals(OcrItem("", "", ""), adapter.getItems()[1])
    }

    @Test
    fun `OCR候補の値を補正するとgetItemsに反映される`() {
        val adapter = OcrItemAdapter(mutableListOf(OcrItem("体重", "88.5", "kg")))
        val holder = createHolder(adapter)
        adapter.onBindViewHolder(holder, 0)

        // 誤読 88.5 → 68.5 に補正
        holder.itemView.findViewById<EditText>(R.id.et_value).setText("68.5")
        assertEquals(OcrItem("体重", "68.5", "kg"), adapter.getItems()[0])
    }

    @Test
    fun `追加した行に入力するとgetItemsに反映される`() {
        val adapter = OcrItemAdapter(mutableListOf(OcrItem("体重", "68.5", "kg")))
        adapter.addItem()
        val holder = createHolder(adapter)
        adapter.onBindViewHolder(holder, 1)

        holder.itemView.findViewById<EditText>(R.id.et_item_name).setText("身長")
        holder.itemView.findViewById<EditText>(R.id.et_value).setText("170")
        holder.itemView.findViewById<EditText>(R.id.et_unit).setText("cm")
        assertEquals(OcrItem("身長", "170", "cm"), adapter.getItems()[1])
    }
}
