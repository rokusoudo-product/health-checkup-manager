package com.rokusodo.healthcheckup.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rokusodo.healthcheckup.databinding.FragmentContactBinding

/**
 * S-07 お問い合わせ画面。お名前・お問い合わせ内容を入力し、mailto Intent でメール作成する。
 * TODO(Phase6): 入力フォーム・mailto Intent・メーラー不在エラー処理を実装する
 */
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
