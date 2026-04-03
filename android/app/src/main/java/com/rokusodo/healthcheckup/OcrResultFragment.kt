package com.rokusodo.healthcheckup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusodo.healthcheckup.databinding.FragmentOcrResultBinding

/**
 * OCR結果を表示し、各行（項目名・値・単位）を編集可能なフォームで提示するフラグメント。
 * 薬事法対応: 医療アドバイス・改善提案のテキストを一切含まない。
 */
class OcrResultFragment : Fragment() {

    private var _binding: FragmentOcrResultBinding? = null
    private val binding get() = _binding!!

    private val args: OcrResultFragmentArgs by navArgs()
    private lateinit var ocrItemAdapter: OcrItemAdapter

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

        val rawPayload = args.ocrText
        val (ocrText, errorType) = parsePayload(rawPayload)

        setupErrorMessage(errorType)
        setupRecyclerView(ocrText)
        setupSaveButton()
    }

    /**
     * CameraFragment からのペイロードを解析し、OCRテキストとエラー種別に分離する。
     */
    private fun parsePayload(payload: String): Pair<String, OcrAnalyzer.OcrError> {
        return when {
            payload.startsWith(CameraFragment.ERROR_PREFIX_INSUFFICIENT) -> {
                val text = payload.removePrefix(CameraFragment.ERROR_PREFIX_INSUFFICIENT)
                Pair(text, OcrAnalyzer.OcrError.INSUFFICIENT_TEXT)
            }
            payload.startsWith(CameraFragment.ERROR_PREFIX_LOW_CONFIDENCE) -> {
                val text = payload.removePrefix(CameraFragment.ERROR_PREFIX_LOW_CONFIDENCE)
                Pair(text, OcrAnalyzer.OcrError.LOW_CONFIDENCE)
            }
            else -> Pair(payload, OcrAnalyzer.OcrError.NONE)
        }
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

    private fun setupRecyclerView(ocrText: String) {
        val items = OcrParser.parse(ocrText).toMutableList()
        ocrItemAdapter = OcrItemAdapter(items)

        binding.recyclerOcrItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ocrItemAdapter
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val items = ocrItemAdapter.getItems()
            // Sprint 2 で Room DB への保存を実装する
            Toast.makeText(
                requireContext(),
                "保存機能はSprint 2で実装予定です（${items.size}件）",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
