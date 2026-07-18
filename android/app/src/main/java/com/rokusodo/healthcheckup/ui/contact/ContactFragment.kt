package com.rokusodo.healthcheckup.ui.contact

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.databinding.FragmentContactBinding

/**
 * S-07 お問い合わせ画面。
 * お名前・お問い合わせ内容を入力し、「送信」で mailto Intent により端末のメールアプリを
 * 起動する（決定Q9: サーバーサイド送信なし）。宛先は strings.xml の contact_email。
 */
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSend.setOnClickListener { sendViaMailer() }
    }

    private fun sendViaMailer() {
        val body = binding.etBody.text?.toString()?.trim().orEmpty()
        if (body.isBlank()) {
            Toast.makeText(requireContext(), R.string.msg_contact_body_required, Toast.LENGTH_SHORT).show()
            return
        }
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val email = getString(R.string.contact_email)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_mail_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_mail_body, name, body))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // メーラー不在端末向けのフォールバック案内（DESIGN.md: エラー状態）
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_no_mailer, email),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
