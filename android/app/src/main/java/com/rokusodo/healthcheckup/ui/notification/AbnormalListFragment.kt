package com.rokusodo.healthcheckup.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusodo.healthcheckup.HealthCheckupApp
import com.rokusodo.healthcheckup.databinding.FragmentAbnormalListBinding
import kotlinx.coroutines.launch

/**
 * 基準値外項目一覧画面。
 * isAbnormal = true の全検査項目を表示する。
 * 薬事法対応: 「要注意」「危険」などの医療判断テキストは含まない。
 */
class AbnormalListFragment : Fragment() {

    private var _binding: FragmentAbnormalListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AbnormalListViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        AbnormalListViewModel.Factory(app.repository)
    }

    private lateinit var adapter: AbnormalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbnormalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeAbnormalItems()
    }

    private fun setupRecyclerView() {
        adapter = AbnormalListAdapter()
        binding.recyclerAbnormalItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@AbnormalListFragment.adapter
        }
    }

    private fun observeAbnormalItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.abnormalItems.collect { items ->
                    if (items.isEmpty()) {
                        binding.tvEmptyAbnormal.visibility = View.VISIBLE
                        binding.recyclerAbnormalItems.visibility = View.GONE
                    } else {
                        binding.tvEmptyAbnormal.visibility = View.GONE
                        binding.recyclerAbnormalItems.visibility = View.VISIBLE
                        adapter.submitList(items)
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
