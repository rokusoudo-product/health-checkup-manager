package com.rokusodo.healthcheckup.ui.itemlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rokusodo.healthcheckup.databinding.FragmentItemListBinding

/**
 * S-03 項目一覧画面。検査項目をカテゴリ色分けで一覧表示し、お気に入り管理を行う。
 * TODO(Phase4): カテゴリ色分け・♥お気に入り・グラフ遷移を実装する
 */
class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
