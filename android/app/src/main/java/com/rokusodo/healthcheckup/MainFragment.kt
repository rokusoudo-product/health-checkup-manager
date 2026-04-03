package com.rokusodo.healthcheckup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rokusodo.healthcheckup.databinding.FragmentMainBinding

/**
 * ホーム画面（診断記録一覧）の仮置きフラグメント。
 * Sprint 2 で本実装（Room DB 連携・一覧表示）を行う。
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_camera)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
