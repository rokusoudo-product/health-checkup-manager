package com.rokusodo.healthcheckup.ui.manualentry

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.rokusodo.healthcheckup.HealthCheckupApp
import com.rokusodo.healthcheckup.OcrItem
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.data.db.HealthCheckupDatabase
import com.rokusodo.healthcheckup.data.db.entity.ItemCategories
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster
import com.rokusodo.healthcheckup.databinding.FragmentManualEntryBinding
import com.rokusodo.healthcheckup.databinding.ItemManualEntryBinding
import com.rokusodo.healthcheckup.ui.common.CategoryColors
import com.rokusodo.healthcheckup.ui.ocrresult.OcrResultViewModel
import com.rokusodo.healthcheckup.ui.ocrresult.SaveState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * S-06b 手入力フォーム画面。
 * 項目マスタ順の入力欄＋単位表示（カテゴリ色分け）、BMI自動計算＋手動上書き可（決定Q8）、
 * ⊕項目追加。保存は OcrResultViewModel（Room+Firestore＋基準値外通知）を共用する。
 */
class ManualEntryFragment : Fragment() {

    private var _binding: FragmentManualEntryBinding? = null
    private val binding get() = _binding!!

    // 保存処理（基準値外通知含む）は OCR確認画面と同一のため ViewModel を共用する
    private val viewModel: OcrResultViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        OcrResultViewModel.Factory(app, app.repository)
    }

    /** 項目名 → 行ビュー。表示順を保持するため LinkedHashMap */
    private val rows = LinkedHashMap<String, ItemManualEntryBinding>()

    /** BMI 手動上書きフラグ（決定Q8: 上書き後は自動計算しない。クリアで自動再開） */
    private var bmiManuallyEdited = false
    private var settingBmiProgrammatically = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildRowsFromMasters()
        setupButtons()
        observeSaveState()
    }

    private fun buildRowsFromMasters() {
        val app = requireActivity().application as HealthCheckupApp
        viewLifecycleOwner.lifecycleScope.launch {
            val masters = app.repository.getAllMasters().first()
            if (rows.isNotEmpty()) return@launch
            sortForEntry(masters).forEach { addRow(it) }
            setupBmiAutoCalc()
        }
    }

    /** カテゴリ順 → シード定義順 → 項目名順（身長→体重→BMI…の自然な並びを保つ） */
    private fun sortForEntry(masters: List<ItemMaster>): List<ItemMaster> {
        val seedOrder = HealthCheckupDatabase.DEFAULT_ITEM_MASTERS
            .mapIndexed { index, master -> master.itemName to index }
            .toMap()
        return masters.sortedWith(
            compareBy(
                { ItemCategories.order(it.category) },
                { seedOrder[it.itemName] ?: Int.MAX_VALUE },
                { it.itemName }
            )
        )
    }

    private fun addRow(master: ItemMaster): ItemManualEntryBinding {
        val row = ItemManualEntryBinding.inflate(layoutInflater, binding.containerEntries, false)
        val categoryColor = ContextCompat.getColor(
            requireContext(), CategoryColors.colorRes(master.category)
        )
        row.tvCategory.text = master.category
        row.tvCategory.setTextColor(categoryColor)
        row.tvItemName.text = master.itemName
        row.tvItemName.setTextColor(categoryColor)
        row.tvUnit.text = master.unit
        binding.containerEntries.addView(row.root)
        rows[master.itemName] = row
        return row
    }

    /** 身長・体重の入力から BMI を自動計算する（BMI欄が手動上書きされていない間のみ） */
    private fun setupBmiAutoCalc() {
        val heightEdit = rows["身長"]?.etValue
        val weightEdit = rows["体重"]?.etValue
        val bmiEdit = rows["BMI"]?.etValue ?: return

        val recalcWatcher = simpleWatcher {
            if (bmiManuallyEdited) return@simpleWatcher
            val bmi = BmiCalculator.calculate(
                heightEdit?.text?.toString(), weightEdit?.text?.toString()
            )
            settingBmiProgrammatically = true
            bmiEdit.setText(bmi ?: "")
            settingBmiProgrammatically = false
        }
        heightEdit?.addTextChangedListener(recalcWatcher)
        weightEdit?.addTextChangedListener(recalcWatcher)

        // BMI欄をユーザーが直接編集したら自動計算を止める。空に戻したら自動計算を再開
        bmiEdit.addTextChangedListener(simpleWatcher { text ->
            if (!settingBmiProgrammatically) {
                bmiManuallyEdited = text.isNotBlank()
            }
        })
    }

    private fun setupButtons() {
        binding.btnAddItem.setOnClickListener { showAddItemDialog() }
        binding.btnSubmit.setOnClickListener {
            if (collectItems().isEmpty()) {
                Toast.makeText(requireContext(), R.string.msg_no_entry_values, Toast.LENGTH_SHORT).show()
            } else {
                showDatePickerDialog()
            }
        }
    }

    /** ⊕項目追加: 項目名・単位を入力して任意項目の行を追加する（カテゴリ=その他） */
    private fun showAddItemDialog() {
        val context = requireContext()
        val nameEdit = EditText(context).apply { hint = getString(R.string.hint_item_name) }
        val unitEdit = EditText(context).apply { hint = getString(R.string.hint_unit) }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(nameEdit)
            addView(unitEdit)
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_title_add_item)
            .setView(container)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val name = nameEdit.text?.toString()?.trim().orEmpty()
                if (name.isNotBlank() && !rows.containsKey(name)) {
                    val row = addRow(
                        ItemMaster(
                            itemName = name,
                            unit = unitEdit.text?.toString()?.trim().orEmpty(),
                            referenceMin = null,
                            referenceMax = null,
                            category = ItemCategories.OTHER
                        )
                    )
                    row.etValue.requestFocus()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    /** 値が入力されている行のみを保存対象にする */
    private fun collectItems(): List<OcrItem> = rows.mapNotNull { (itemName, row) ->
        val value = row.etValue.text?.toString()?.trim().orEmpty()
        if (value.isBlank()) null
        else OcrItem(itemName = itemName, value = value, unit = row.tvUnit.text.toString())
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                showFacilityInputDialog(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showFacilityInputDialog(date: String) {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.hint_facility)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_facility))
            .setView(editText)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val facility = editText.text?.toString() ?: ""
                viewModel.saveRecord(date, facility, collectItems())
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is SaveState.Idle -> {
                            binding.btnSubmit.isEnabled = true
                            binding.progressSave.visibility = View.GONE
                        }
                        is SaveState.Loading -> {
                            binding.btnSubmit.isEnabled = false
                            binding.progressSave.visibility = View.VISIBLE
                        }
                        is SaveState.Success -> {
                            binding.btnSubmit.isEnabled = true
                            binding.progressSave.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.msg_save_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetState()
                            // 登録完了 → S-02 ホームへ戻る（刷新001の遷移図）
                            findNavController().popBackStack(R.id.homeFragment, false)
                        }
                        is SaveState.Error -> {
                            binding.btnSubmit.isEnabled = true
                            binding.progressSave.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.msg_save_error, state.message),
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun simpleWatcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            onChange(s?.toString() ?: "")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
