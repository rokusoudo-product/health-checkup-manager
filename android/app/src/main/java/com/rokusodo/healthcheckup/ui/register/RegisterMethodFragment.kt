package com.rokusodo.healthcheckup.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.databinding.FragmentRegisterMethodBinding

/**
 * S-05 登録方法選択画面。カメラ読み取り（S-06a）か手入力（S-06b）の二択。
 */
class RegisterMethodFragment : Fragment() {

    private var _binding: FragmentRegisterMethodBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCamera.setOnClickListener {
            findNavController().navigate(R.id.action_register_method_to_camera)
        }
        binding.btnManual.setOnClickListener {
            findNavController().navigate(R.id.action_register_method_to_manual_entry)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
