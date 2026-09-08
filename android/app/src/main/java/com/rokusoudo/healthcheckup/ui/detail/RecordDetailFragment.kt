package com.rokusoudo.healthcheckup.ui.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.rokusoudo.healthcheckup.HealthCheckupApp
import com.rokusoudo.healthcheckup.R
import com.rokusoudo.healthcheckup.databinding.FragmentRecordDetailBinding
import kotlinx.coroutines.launch

/**
 * 診断記録詳細画面。
 * 検査項目一覧を表示し、基準値外の項目は赤くハイライトする。
 * 各項目行の「グラフ」ボタンで経年グラフ画面へ遷移できる。
 * オーバーフローメニューから記録単位の削除もできる（Issue #47）。
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
        setupMenu()
        observeDeleteState()
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

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_record_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_record -> {
                        showDeleteConfirmDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Issue #47 受け入れ基準: 削除は確認ダイアログを必須にする。
     * 誤操作防止のため既定フォーカスはキャンセル側に置く（Issue #34のアカウント削除確認と同方針）。
     * 「削除する」ボタンは DESIGN.md の危険操作トークン（error/status_warning。基準値外ハイライトと同一）で強調する。
     */
    private fun showDeleteConfirmDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_delete_record)
            .setMessage(R.string.dialog_message_delete_record)
            .setPositiveButton(R.string.btn_delete_record) { _, _ -> viewModel.deleteRecord() }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.requestFocus()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning))
        }
        dialog.show()
    }

    private fun observeDeleteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.deleteState.collect { state ->
                    when (state) {
                        is RecordDetailViewModel.DeleteState.Success -> {
                            Toast.makeText(requireContext(), R.string.msg_delete_record_success, Toast.LENGTH_SHORT)
                                .show()
                            // 削除後は記録一覧（呼び出し元のMainFragment）へ戻る
                            findNavController().popBackStack()
                        }
                        is RecordDetailViewModel.DeleteState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.msg_delete_record_error, state.message.orEmpty()),
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.consumeErrorState()
                        }
                        else -> Unit
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
