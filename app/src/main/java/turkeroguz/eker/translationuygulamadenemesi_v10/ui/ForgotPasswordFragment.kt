package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import turkeroguz.eker.translationuygulamadenemesi_v10.R

class ForgotPasswordFragment : Fragment() {

    private lateinit var cardStep1: MaterialCardView // Mail Gönderme Kartı
    private lateinit var cardStep2: MaterialCardView // Şifre Değiştirme Kartı

    private lateinit var etResetEmail: TextInputEditText
    private lateinit var btnSendResetCode: Button

    private lateinit var etNewPassword: TextInputEditText
    private lateinit var btnUpdatePassword: Button

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvBackToLogin: TextView

    // Firebase'den gelen özel kod (Linkten alınacak)
    private var oobCode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MainActivity'den gelen kodu al
        oobCode = arguments?.getString("oobCode")

        // View Tanımlamaları
        cardStep1 = view.findViewById(R.id.cardStep1)
        cardStep2 = view.findViewById(R.id.cardStep2)
        etResetEmail = view.findViewById(R.id.etResetEmail)
        btnSendResetCode = view.findViewById(R.id.btnSendResetCode)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        // --- EKRAN AYARLAMASI ---
        if (oobCode != null) {
            // Eğer linkten geldiyse direkt ŞİFRE DEĞİŞTİRME ekranını aç
            showChangePasswordScreen()
        } else {
            // Normal geldiyse MAİL GİRME ekranını aç
            showEmailScreen()
        }

        // 1. BUTON: Mail Gönder (Link Gönderir)
        btnSendResetCode.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                sendFirebaseLink(email)
            } else {
                etResetEmail.error = "E-posta giriniz"
            }
        }

        // 2. BUTON: Şifreyi Güncelle (Linkten geldiyse çalışır)
        btnUpdatePassword.setOnClickListener {
            val newPass = etNewPassword.text.toString().trim()
            if (newPass.length >= 6) {
                confirmPasswordChange(newPass)
            } else {
                Toast.makeText(context, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun showEmailScreen() {
        cardStep1.visibility = View.VISIBLE
        cardStep2.visibility = View.GONE
        tvTitle.text = "Şifreni mi Unuttun?"
        tvSubtitle.text = "E-posta adresini gir, sana şifreni sıfırlaman için güvenli bir bağlantı gönderelim."
    }

    private fun showChangePasswordScreen() {
        cardStep1.visibility = View.GONE
        cardStep2.visibility = View.VISIBLE

        // Tasarımdaki gereksiz "Kod Gir" kutusunu gizleyelim (Layout'ta kod kutusu varsa id'si etResetCode idi)
        view?.findViewById<View>(R.id.etResetCode)?.visibility = View.GONE

        tvTitle.text = "Yeni Şifre Belirle"
        tvSubtitle.text = "Lütfen yeni ve güçlü bir şifre giriniz."
    }

    private fun sendFirebaseLink(email: String) {
        btnSendResetCode.isEnabled = false
        btnSendResetCode.text = "Gönderiliyor..."

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                btnSendResetCode.text = "Tekrar Gönder"
                btnSendResetCode.isEnabled = true

                android.app.AlertDialog.Builder(context)
                    .setTitle("Bağlantı Gönderildi 🚀")
                    .setMessage("$email adresine bir link gönderdik.\n\nLinke tıkladığında UYGULAMA AÇILACAK ve şifreni buradan değiştirebileceksin.")
                    .setPositiveButton("Tamam", null)
                    .show()
            }
            .addOnFailureListener {
                btnSendResetCode.isEnabled = true
                btnSendResetCode.text = "Doğrulama Linki Gönder"
                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmPasswordChange(newPass: String) {
        if (oobCode == null) return

        btnUpdatePassword.isEnabled = false
        btnUpdatePassword.text = "Güncelleniyor..."

        FirebaseAuth.getInstance().confirmPasswordReset(oobCode!!, newPass)
            .addOnSuccessListener {
                Toast.makeText(context, "Şifreniz Başarıyla Değiştirildi! 🎉", Toast.LENGTH_LONG).show()
                // Giriş Ekranına At
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }
            .addOnFailureListener {
                btnUpdatePassword.isEnabled = true
                btnUpdatePassword.text = "Şifreyi Güncelle"
                Toast.makeText(context, "Süre dolmuş veya hata oluştu. Lütfen tekrar mail isteyin.", Toast.LENGTH_LONG).show()
            }
    }
}