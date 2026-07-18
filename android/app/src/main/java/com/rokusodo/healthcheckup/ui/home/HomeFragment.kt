package com.rokusodo.healthcheckup.ui.home

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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.databinding.FragmentHomeBinding

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

    private fun signOut() {
        // Firebase Auth からサインアウト
        FirebaseAuth.getInstance().signOut()
        // Google Sign-In のキャッシュもクリア
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()
        // ログイン画面へ遷移（バックスタックを消去）
        findNavController().navigate(R.id.action_home_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
