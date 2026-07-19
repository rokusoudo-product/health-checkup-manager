package com.rokusoudo.healthcheckup.ui.master

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusoudo.healthcheckup.HealthCheckupApp
import com.rokusoudo.healthcheckup.R
import com.rokusoudo.healthcheckup.data.db.entity.ItemMaster
import com.rokusoudo.healthcheckup.databinding.FragmentItemMasterBinding
import kotlinx.coroutines.launch

/**
 * 項目マスター管理画面。
 * 検査項目ごとの基準値（参考値）を編集・追加できる。
 * 薬事法対応: この基準値は医療診断に使用しない。表示・ハイライトのみ。
 */
class ItemMasterFragment : Fragment() {

    private var _binding: FragmentItemMasterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemMasterViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        ItemMasterViewModel.Factory(app.repository)
    }

    private lateinit var itemMasterAdapter: ItemMasterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemMasterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeMasters()
    }

    private fun setupRecyclerView() {
        itemMasterAdapter = ItemMasterAdapter { master ->
            showEditDialog(master)
        }
        binding.recyclerItemMasters.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemMasterAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddMaster.setOnClickListener {
            showEditDialog(null)
        }
        applyFabInsets()
    }

    /**
     * targetSdk 35 の edge-to-edge 強制によりFABがナビゲーションバーに被るため、
     * ナビゲーションバー分をボトムマージンに加算する。
     */
    private fun applyFabInsets() {
        val initialMarginBottom =
            (binding.fabAddMaster.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(binding.fabAddMaster) { view, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = initialMarginBottom + navBarInsets.bottom
            view.layoutParams = params
            insets
        }
    }

    private fun observeMasters() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.masters.collect { masters ->
                    itemMasterAdapter.submitList(masters)
                }
            }
        }
    }

    private fun showEditDialog(existing: ItemMaster?) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val etItemName = EditText(context).apply {
            hint = getString(R.string.hint_item_name)
            setText(existing?.itemName ?: "")
            isEnabled = existing == null // 新規のみ項目名を編集可
        }
        val etUnit = EditText(context).apply {
            hint = getString(R.string.hint_unit)
            setText(existing?.unit ?: "")
        }
        val etRefMin = EditText(context).apply {
            hint = getString(R.string.hint_ref_min)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            setText(existing?.referenceMin?.toString() ?: "")
        }
        val etRefMax = EditText(context).apply {
            hint = getString(R.string.hint_ref_max)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            setText(existing?.referenceMax?.toString() ?: "")
        }

        layout.addView(etItemName)
        layout.addView(etUnit)
        layout.addView(etRefMin)
        layout.addView(etRefMax)

        val title = if (existing == null) {
            getString(R.string.dialog_title_add_master)
        } else {
            getString(R.string.dialog_title_edit_master)
        }

        android.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val itemName = etItemName.text.toString().trim()
                if (itemName.isBlank()) return@setPositiveButton
                val master = ItemMaster(
                    itemName = itemName,
                    unit = etUnit.text.toString().trim(),
                    referenceMin = etRefMin.text.toString().toDoubleOrNull(),
                    referenceMax = etRefMax.text.toString().toDoubleOrNull()
                )
                viewModel.upsertMaster(master)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
