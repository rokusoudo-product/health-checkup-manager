package com.rokusodo.healthcheckup.ui.itemlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusodo.healthcheckup.HealthCheckupApp
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.databinding.FragmentItemListBinding
import kotlinx.coroutines.launch

/**
 * S-03 項目一覧画面。検査項目をカテゴリ色分けで一覧表示し、お気に入り管理を行う。
 * 行タップ → S-04（該当項目のグラフ）へ遷移する。
 */
class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemListViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        ItemListViewModel.Factory(app.repository)
    }

    private lateinit var adapter: ItemListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ItemListAdapter(
            onItemClick = { master ->
                findNavController().navigate(
                    R.id.action_item_list_to_trend_graph,
                    bundleOf("itemName" to master.itemName)
                )
            },
            onFavoriteClick = { master -> viewModel.toggleFavorite(master) }
        )
        binding.recyclerItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerItems.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { adapter.submitList(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
