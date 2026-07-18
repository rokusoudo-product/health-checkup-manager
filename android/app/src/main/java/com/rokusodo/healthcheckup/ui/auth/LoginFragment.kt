package com.rokusodo.healthcheckup.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

/**
 * Google SSO によるログイン画面。
 * Firebase Auth の currentUser が null の場合にこの画面を表示する。
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In の結果を受け取るランチャー
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleSignInResult(result.data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleSignIn()
        setupClickListeners()
        observeLoginState()
    }

    private fun setupGoogleSignIn() {
        // TODO: google-services.json に client_type:3 の oauth_client を追加し、
        //       Firebase Console で Web Client ID を設定すること。
        //       現在 google-services.json に Web Client ID が未登録のため空文字を使用。
        val webClientId = "735205368933-ndisk3lrvmgicvss0nfonv3o80nnavll.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupClickListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signIn(idToken)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_sign_in_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_sign_in_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginViewModel.LoginState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGoogleSignIn.isEnabled = true
                        }
                        is LoginViewModel.LoginState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnGoogleSignIn.isEnabled = false
                        }
                        is LoginViewModel.LoginState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            findNavController().navigate(R.id.action_login_to_home)
                        }
                        is LoginViewModel.LoginState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGoogleSignIn.isEnabled = true
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.msg_sign_in_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetState()
                        }
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
