package com.rokusodo.healthcheckup.ui.manualentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rokusodo.healthcheckup.databinding.FragmentManualEntryBinding

/**
 * S-06b 手入力フォーム画面。マスタ順の入力欄＋単位表示、BMI自動計算、⊕項目追加。
 * TODO(Phase5): 入力フォーム・BMI自動計算＋手動上書き・保存を実装する
 */
class ManualEntryFragment : Fragment() {

    private var _binding: FragmentManualEntryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
