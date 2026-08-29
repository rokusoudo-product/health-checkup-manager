package com.rokusoudo.healthcheckup.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.rokusoudo.healthcheckup.R
import com.rokusoudo.healthcheckup.data.account.AccountDeletionProgress
import com.rokusoudo.healthcheckup.databinding.FragmentHomeBinding
import com.rokusoudo.healthcheckup.ui.account.AccountDeletionViewModel
import kotlinx.coroutines.launch

/**
 * S-02 ホーム画面。「登録」「グラフ」「お問い合わせ」への分岐ハブ。
 * 既存4画面（記録一覧・項目マスター・基準値外一覧）とログアウトはオーバーフローメニューから遷移する（決定Q7）。
 * Issue #34: 同じオーバーフローメニューに「アカウントとデータを削除」を追加。
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val deletionViewModel: AccountDeletionViewModel by viewModels()

    // アカウント削除中の再認証要求（FirebaseAuthRecentLoginRequiredException相当）に対応するための
    // Google再サインインランチャー。LoginFragmentの実装と同じ requestIdToken 設定を使う。
    private val reauthGoogleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(requireActivity(), gso)
    }

    private val reauthSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleReauthSignInResult(result.data)
    }

    private var deletionProgressDialog: AlertDialog? = null

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
        observeDeletionState()
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
                    R.id.action_delete_account -> {
                        showDeleteAccountConfirmDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * サインアウトはFirebase Auth・Google Sign-Inのセッションを終了するのみで、
     * クラウド・端末のデータ削除は一切行わない（アカウント削除機能とは明確に区別する。Issue #34）。
     */
    private fun signOut() {
        // Firebase Auth からサインアウト
        FirebaseAuth.getInstance().signOut()
        // Google Sign-In のキャッシュもクリア
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(requireActivity(), gso).signOut()
        // ログイン画面へ遷移（バックスタックを消去）
        findNavController().navigate(R.id.action_home_to_login)
    }

    /**
     * アカウント削除の確認ダイアログ。取り消し不可であることと削除対象を明示し、
     * 既定フォーカスはキャンセル側に置く（誤操作防止。Issue #34 受け入れ基準）。
     */
    private fun showDeleteAccountConfirmDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_delete_account)
            .setMessage(R.string.dialog_message_delete_account)
            .setPositiveButton(R.string.btn_delete_account) { _, _ ->
                deletionViewModel.deleteAccount()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
        dialog.setOnShowListener {
            // 既定はキャンセル側にフォーカス（誤タップでの削除を防ぐ）
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.requestFocus()
        }
        dialog.show()
    }

    private fun observeDeletionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                deletionViewModel.uiState.collect { state ->
                    when (state) {
                        is AccountDeletionViewModel.DeletionUiState.Idle -> {
                            dismissProgressDialog()
                        }
                        is AccountDeletionViewModel.DeletionUiState.InProgress -> {
                            showProgressDialog()
                        }
                        is AccountDeletionViewModel.DeletionUiState.Success -> {
                            dismissProgressDialog()
                            onDeletionSuccess()
                        }
                        is AccountDeletionViewModel.DeletionUiState.ReauthRequired -> {
                            dismissProgressDialog()
                            showReauthRequiredDialog()
                        }
                        is AccountDeletionViewModel.DeletionUiState.Error -> {
                            dismissProgressDialog()
                            showDeletionErrorDialog(state.progress, state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showProgressDialog() {
        if (deletionProgressDialog?.isShowing == true) return
        deletionProgressDialog = AlertDialog.Builder(requireContext())
            .setMessage(R.string.msg_deleting_account)
            .setCancelable(false)
            .show()
    }

    private fun dismissProgressDialog() {
        deletionProgressDialog?.dismiss()
        deletionProgressDialog = null
    }

    private fun onDeletionSuccess() {
        Toast.makeText(requireContext(), R.string.msg_delete_account_success, Toast.LENGTH_SHORT).show()
        // Firebase Authのユーザーは既に削除済みだが、Google Sign-Inのキャッシュも念のためクリアする
        reauthGoogleSignInClient.signOut()
        deletionViewModel.resetState()
        findNavController().navigate(R.id.action_home_to_login)
    }

    /** 再認証要求（FirebaseAuthRecentLoginRequiredException相当）時: Google再サインインを促す。 */
    private fun showReauthRequiredDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_reauth_required)
            .setMessage(R.string.dialog_message_reauth_required)
            .setPositiveButton(R.string.btn_reauth_and_retry) { _, _ ->
                val signInIntent: Intent = reauthGoogleSignInClient.signInIntent
                reauthSignInLauncher.launch(signInIntent)
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ -> deletionViewModel.resetState() }
            .show()
    }

    private fun handleReauthSignInResult(data: Intent?) {
        // Google再サインインそのものが失敗・キャンセルされた場合でも、
        // ここまでに完了済みの削除進捗（Firestore・Room）を「未削除」で誤上書きしないよう、
        // ViewModel側で保持している最新の進捗を使う（onReauthSignInFailed経由）。
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                deletionViewModel.reauthenticateThenRetry(idToken)
            } else {
                deletionViewModel.onReauthSignInFailed(getString(R.string.msg_sign_in_failed))
            }
        } catch (e: ApiException) {
            deletionViewModel.onReauthSignInFailed(getString(R.string.msg_sign_in_failed))
        }
    }

    /**
     * 途中失敗時: 何が削除され、何が残ったかを明示してサイレント失敗させない（Issue #34 受け入れ基準）。
     */
    private fun showDeletionErrorDialog(progress: AccountDeletionProgress, message: String) {
        fun status(done: Boolean) = getString(
            if (done) R.string.label_deletion_status_done else R.string.label_deletion_status_pending
        )
        val detail = getString(
            R.string.msg_delete_account_progress_detail,
            getString(R.string.label_deletion_target_cloud), status(progress.cloudDataDeleted),
            getString(R.string.label_deletion_target_local), status(progress.localDataDeleted),
            getString(R.string.label_deletion_target_auth), status(progress.authAccountDeleted)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_delete_account_error)
            .setMessage(getString(R.string.msg_delete_account_error, message, detail))
            .setPositiveButton(R.string.btn_cancel) { _, _ -> deletionViewModel.resetState() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissProgressDialog()
        _binding = null
    }
}
