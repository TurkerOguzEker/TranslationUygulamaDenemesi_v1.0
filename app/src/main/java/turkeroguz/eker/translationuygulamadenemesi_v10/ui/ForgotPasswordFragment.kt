package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import turkeroguz.eker.translationuygulamadenemesi_v10.R

class ForgotPasswordFragment : Fragment() {

    private lateinit var etResetEmail: TextInputEditText
    private lateinit var btnSendLink: Button
    private lateinit var tvBackToLogin: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etResetEmail = view.findViewById(R.id.etResetEmail)
        btnSendLink = view.findViewById(R.id.btnSendLink)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        btnSendLink.setOnClickListener {
            val email = etResetEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                sendResetLink(email)
            } else {
                etResetEmail.error = "Lütfen e-posta adresinizi girin."
            }
        }

        tvBackToLogin.setOnClickListener {
            // Giriş ekranına geri dön (LoginFragment)
            parentFragmentManager.popBackStack()
        }
    }

    private fun sendResetLink(email: String) {
        btnSendLink.isEnabled = false
        btnSendLink.text = "Gönderiliyor..."

        val auth = FirebaseAuth.getInstance()

        // ✅ KRİTİK DEĞİŞİKLİK: TÜM DİLLERİ DESTEKLE
        // Telefonun dili neyse (Türkçe, İngilizce, Almanca...) mail ve site o dilde açılır.
        auth.useAppLanguage()

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Başarılı
                showSuccessDialog(email)
                btnSendLink.isEnabled = true
                btnSendLink.text = "Tekrar Gönder"
            }
            .addOnFailureListener { e ->
                // Hata
                btnSendLink.isEnabled = true
                btnSendLink.text = "Sıfırlama Bağlantısı Gönder"

                // Hata mesajını genel bir formatta göster (veya e.localizedMessage kullan)
                Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showSuccessDialog(email: String) {
        // Dialog metinleri de genel olmalı veya string.xml'den çekilmeli ama
        // şimdilik Türkçe bırakıyorum, string.xml ile çoklu dil yapabilirsin.
        AlertDialog.Builder(context)
            .setTitle("E-posta Gönderildi 🚀")
            .setMessage("$email adresine sıfırlama bağlantısı gönderildi.\n\n1. Maildeki linke tıkla.\n2. Açılan tarayıcıda yeni şifreni belirle.\n3. Uygulamaya dönüp yeni şifrenle giriş yap.")
            .setPositiveButton("Tamam") { _, _ ->
                parentFragmentManager.popBackStack() // Login'e dön
            }
            .setCancelable(false)
            .show()
    }
}