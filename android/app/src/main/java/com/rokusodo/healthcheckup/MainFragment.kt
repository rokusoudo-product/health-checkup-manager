package com.rokusodo.healthcheckup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.rokusodo.healthcheckup.databinding.FragmentMainBinding
import com.rokusodo.healthcheckup.ui.main.MainViewModel
import com.rokusodo.healthcheckup.ui.main.RecordListAdapter
import kotlinx.coroutines.launch

/**
 * ホーム画面（診断記録一覧）フラグメント。
 * ExaminationRecord 一覧を RecyclerView で表示する。
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
        setupMenu()
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
            findNavController().navigate(R.id.action_main_to_camera)
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

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_item_master -> {
                        findNavController().navigate(R.id.action_main_to_item_master)
                        true
                    }
                    R.id.action_abnormal_list -> {
                        findNavController().navigate(R.id.action_main_to_abnormal_list)
                        true
                    }
                    R.id.action_sign_out -> {
                        signOut()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun signOut() {
        // Firebase Auth からサインアウト
        FirebaseAuth.getInstance().signOut()
        // Google Sign-In のキャッシュもクリア
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()
        // ログイン画面へ遷移（バックスタックを消去）
        findNavController().navigate(R.id.action_main_to_login)
    }

    private fun observeRecords() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.records.collect { records ->
                    if (records.isEmpty()) {
                        binding.tvEmptyMessage.visibility = View.VISIBLE
                        binding.recyclerRecords.visibility = View.GONE
                    } else {
                        binding.tvEmptyMessage.visibility = View.GONE
                        binding.recyclerRecords.visibility = View.VISIBLE
                        val items = records.map { record ->
                            RecordListAdapter.RecordWithAbnormalCount(
                                record = record,
                                abnormalCount = 0 // 詳細なカウントは別途Flow取得が必要なため0で初期化
                            )
                        }
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
