package com.rokusoudo.healthcheckup.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusoudo.healthcheckup.HealthCheckupApp
import com.rokusoudo.healthcheckup.databinding.FragmentRecordDetailBinding
import kotlinx.coroutines.launch

/**
 * 診断記録詳細画面。
 * 検査項目一覧を表示し、基準値外の項目は赤くハイライトする。
 * 各項目行の「グラフ」ボタンで経年グラフ画面へ遷移できる。
 * 薬事法対応: 診断・アドバイス・医療判断のテキストを一切含まない。
 */
class RecordDetailFragment : Fragment() {

    private var _binding: FragmentRecordDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RecordDetailFragmentArgs by navArgs()

    private val viewModel: RecordDetailViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        RecordDetailViewModel.Factory(app.repository, args.recordId)
    }

    private lateinit var recordItemAdapter: RecordItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeItems()
    }

    private fun setupRecyclerView() {
        recordItemAdapter = RecordItemAdapter { itemName ->
            val action = RecordDetailFragmentDirections.actionRecordDetailToTrendGraph(itemName)
            findNavController().navigate(action)
        }
        binding.recyclerExaminationItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordItemAdapter
        }
    }

    private fun observeItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { items ->
                    recordItemAdapter.submitList(items)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
