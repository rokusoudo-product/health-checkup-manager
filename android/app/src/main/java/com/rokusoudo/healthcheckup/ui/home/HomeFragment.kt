package com.rokusoudo.healthcheckup.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.rokusoudo.healthcheckup.HealthCheckupApp
import com.rokusoudo.healthcheckup.R
import com.rokusoudo.healthcheckup.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

/**
 * S-02 ホーム画面。「登録」「グラフ」「お問い合わせ」への分岐ハブ。
 * 既存4画面（記録一覧・項目マスター・基準値外一覧）とログアウトはオーバーフローメニューから遷移する（決定Q7）。
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        setupMenu()
    }

    private fun setupButtons() {
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_register_method)
        }
        binding.btnGraph.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_item_list)
        }
        binding.btnContact.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_contact)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_record_list -> {
                        findNavController().navigate(R.id.action_home_to_record_list)
                        true
                    }
                    R.id.action_item_master -> {
                        findNavController().navigate(R.id.action_home_to_item_master)
                        true
                    }
                    R.id.action_abnormal_list -> {
                        findNavController().navigate(R.id.action_home_to_abnormal_list)
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

    /**
     * Issue #41: サインアウト時、共用端末に前の利用者の健診データが残らないよう
     * 端末の Room DB もあわせて消去する（削除確認ダイアログ等は不要。既存UXは変えない）。
     * Room クリアは suspend 処理のため、ライフサイクルに紐づくコルーチンから呼び出す。
     */
    private fun signOut() {
        val repository = (requireActivity().application as HealthCheckupApp).repository
        viewLifecycleOwner.lifecycleScope.launch {
            // 端末の Room DB から健診データを消去（Firestore上のデータには影響しない）
            repository.clearLocalDataOnSignOut()
            // Firebase Auth からサインアウト
            FirebaseAuth.getInstance().signOut()
            // Google Sign-In のキャッシュもクリア
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(requireActivity(), gso).signOut()
            // ログイン画面へ遷移（バックスタックを消去）
            findNavController().navigate(R.id.action_home_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
