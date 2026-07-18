package com.rokusodo.healthcheckup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusodo.healthcheckup.databinding.FragmentMainBinding
import com.rokusodo.healthcheckup.ui.main.MainViewModel
import com.rokusodo.healthcheckup.ui.main.RecordListAdapter
import kotlinx.coroutines.launch

/**
 * 診断記録一覧フラグメント（旧ホーム）。
 * 刷新001以降は S-02 ホームのメニューから遷移する（決定Q7）。
 * メニュー（項目マスター・基準値外一覧・ログアウト）はホーム側に移設済み。
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        MainViewModel.Factory(app.repository)
    }

    private lateinit var recordListAdapter: RecordListAdapter

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

        setupRecyclerView()
        setupFab()
        observeRecords()
    }

    private fun setupRecyclerView() {
        recordListAdapter = RecordListAdapter { record ->
            val action = MainFragmentDirections.actionMainToRecordDetail(record.id)
            findNavController().navigate(action)
        }
        binding.recyclerRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordListAdapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // 刷新001: 登録方法選択（S-05）を経由して登録する
            findNavController().navigate(R.id.action_main_to_register_method)
        }
        applyFabInsets()
    }

    /**
     * targetSdk 35 の edge-to-edge 強制によりFABがナビゲーションバーに被るため、
     * ナビゲーションバー分をボトムマージンに加算する。
     */
    private fun applyFabInsets() {
        val initialMarginBottom = (binding.fabAdd.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(binding.fabAdd) { view, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = initialMarginBottom + navBarInsets.bottom
            view.layoutParams = params
            insets
        }
    }

    private fun observeRecords() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.records.collect { items ->
                    if (items.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.recyclerRecords.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.recyclerRecords.visibility = View.VISIBLE
                        recordListAdapter.submitList(items)
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
