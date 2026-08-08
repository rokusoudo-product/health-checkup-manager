package com.rokusoudo.healthcheckup

import android.Manifest
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusoudo.healthcheckup.databinding.FragmentOcrResultBinding
import com.rokusoudo.healthcheckup.ui.ocrresult.OcrResultViewModel
import com.rokusoudo.healthcheckup.ui.ocrresult.SaveState
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * OCR結果を表示し、各行（項目名・値・単位）を編集可能なフォームで提示するフラグメント。
 * 薬事法対応: 医療アドバイス・改善提案のテキストを一切含まない。
 */
class OcrResultFragment : Fragment() {

    private var _binding: FragmentOcrResultBinding? = null
    private val binding get() = _binding!!

    private val args: OcrResultFragmentArgs by navArgs()
    private lateinit var ocrItemAdapter: OcrItemAdapter

    private val viewModel: OcrResultViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        OcrResultViewModel.Factory(app, app.repository)
    }

    // Android 13+ 通知パーミッションリクエスト
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // パーミッション結果は無視（通知は任意機能のため）
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOcrResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Android 13 以上の場合、通知パーミッションをリクエスト
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Issue #12: CameraFragment からは座標付きセル（OcrCell[]）とエラー種別（enum name）を
        // 別々の引数として受け取る（従来のテキスト+プレフィクスの合成方式から変更）。
        val cells = args.ocrCells.toList()
        val errorType = runCatching { OcrAnalyzer.OcrError.valueOf(args.ocrError) }
            .getOrDefault(OcrAnalyzer.OcrError.NONE)

        setupErrorMessage(errorType)
        setupRecyclerView(cells)
        setupSaveButton()
        observeSaveState()
    }

    private fun setupErrorMessage(errorType: OcrAnalyzer.OcrError) {
        val message = when (errorType) {
            OcrAnalyzer.OcrError.INSUFFICIENT_TEXT ->
                getString(R.string.error_insufficient_text)
            OcrAnalyzer.OcrError.LOW_CONFIDENCE ->
                getString(R.string.error_low_confidence)
            OcrAnalyzer.OcrError.NONE -> null
        }

        if (message != null) {
            binding.tvErrorMessage.text = message
            binding.tvErrorMessage.visibility = View.VISIBLE
        } else {
            binding.tvErrorMessage.visibility = View.GONE
        }
    }

    // Issue #14: 複数列の健診表（今回/前回/前々回）から「今回」列を特定するための
    // グリッド。列選択ダイアログでユーザーが選んだ列から項目リストを再構築する際に使う。
    private var ocrGrid: List<List<String>> = emptyList()

    private fun setupRecyclerView(cells: List<OcrCell>) {
        ocrGrid = OcrParser.buildGrid(cells)
        when (val selection = OcrColumnSelector.selectValueColumn(ocrGrid)) {
            is ColumnSelectionResult.Resolved -> {
                bindItems(OcrColumnSelector.rebuildItems(ocrGrid, selection.valueColumnIndex))
            }
            is ColumnSelectionResult.NeedsUserSelection -> {
                // 自動判定に失敗した場合は空のプレースホルダーを表示しつつ、
                // 列選択ダイアログで選ばれた列の値でリストを再構築する。
                bindItems(listOf(OcrItem("", "", "")))
                showColumnSelectionDialog(selection.candidates)
            }
        }
    }

    private fun bindItems(items: List<OcrItem>) {
        ocrItemAdapter = OcrItemAdapter(items.toMutableList())
        binding.recyclerOcrItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ocrItemAdapter
        }
    }

    /**
     * 複数列の健診表で「今回」列を自動判定できなかった場合に表示する列選択ダイアログ。
     * 各候補列のヘッダーラベルとプレビュー値（先頭数項目）を提示し、ユーザーが選んだ列で
     * 項目リストを再構築する（Issue #14 受け入れ基準）。
     */
    private fun showColumnSelectionDialog(candidates: List<ColumnCandidate>) {
        val labels = candidates.mapIndexed { index, candidate ->
            val headerPart = candidate.headerLabel.ifBlank {
                getString(R.string.label_column_fallback, index + 1)
            }
            val previewPart = if (candidate.previewValues.isEmpty()) {
                getString(R.string.label_column_no_preview)
            } else {
                candidate.previewValues.joinToString("、")
            }
            getString(R.string.label_column_item, headerPart, previewPart)
        }.toTypedArray()

        var selectedIndex = 0
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_column_selection))
            .setSingleChoiceItems(labels, selectedIndex) { _, which -> selectedIndex = which }
            .setPositiveButton(getString(R.string.btn_select_column)) { _, _ ->
                val chosenColumn = candidates[selectedIndex].columnIndex
                bindItems(OcrColumnSelector.rebuildItems(ocrGrid, chosenColumn))
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            showDatePickerDialog()
        }
        // ⊕項目追加（刷新001 S-06a）
        binding.btnAddItem.setOnClickListener {
            ocrItemAdapter.addItem()
            binding.recyclerOcrItems.scrollToPosition(ocrItemAdapter.itemCount - 1)
        }
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
        val editText = android.widget.EditText(requireContext()).apply {
            hint = getString(R.string.hint_facility)
        }
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_facility))
            .setView(editText)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val facility = editText.text?.toString() ?: ""
                val items = ocrItemAdapter.getItems()
                viewModel.saveRecord(date, facility, items)
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
                            binding.btnSave.isEnabled = true
                            binding.progressSave.visibility = View.GONE
                        }
                        is SaveState.Loading -> {
                            binding.btnSave.isEnabled = false
                            binding.progressSave.visibility = View.VISIBLE
                        }
                        is SaveState.Success -> {
                            binding.btnSave.isEnabled = true
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
                            binding.btnSave.isEnabled = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
