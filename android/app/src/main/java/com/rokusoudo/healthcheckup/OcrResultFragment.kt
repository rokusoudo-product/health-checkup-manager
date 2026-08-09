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

    // Issue #13: ヘッダー行（検査日が並ぶ行）から取得できた検査日。
    // 取得できた場合は検査日入力ダイアログの初期値として使用する。取得できなければnull（従来どおり手動入力）。
    private var detectedExamDate: String? = null

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
        observeMasterMatchResult()
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

    private fun setupRecyclerView(cells: List<OcrCell>) {
        val parseResult = OcrParser.parseWithDate(cells)
        detectedExamDate = parseResult.examDate
        val items = parseResult.items.toMutableList()
        ocrItemAdapter = OcrItemAdapter(items)

        binding.recyclerOcrItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ocrItemAdapter
        }

        // Issue #15: 項目マスタとの照合（非同期）を開始する。結果は observeMasterMatchResult で反映する。
        viewModel.matchItemsAgainstMaster(parseResult.items)
    }

    /**
     * 項目マスタ照合結果（Issue #15）を反映する。
     * 照合済みの項目名・単位でRecyclerViewを更新し、除外した行数を確認画面に表示する。
     */
    private fun observeMasterMatchResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.masterMatchResult.collect { result ->
                    if (result == null) return@collect

                    ocrItemAdapter.submitItems(result.items)

                    if (result.excludedCount > 0) {
                        binding.tvExcludedCount.text =
                            getString(R.string.msg_items_excluded, result.excludedCount)
                        binding.tvExcludedCount.visibility = View.VISIBLE
                    } else {
                        binding.tvExcludedCount.visibility = View.GONE
                    }
                }
            }
        }
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
        // Issue #13: ヘッダー行から検査日が取得できていれば、検査日入力ダイアログの初期値に反映する。
        // 取得できていない場合は従来どおり今日の日付が初期値になり、ユーザーが手動で選択する。
        detectedExamDate?.let { dateString ->
            runCatching {
                val (year, month, day) = dateString.split("-").map { it.toInt() }
                calendar.set(year, month - 1, day)
            }
        }
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
