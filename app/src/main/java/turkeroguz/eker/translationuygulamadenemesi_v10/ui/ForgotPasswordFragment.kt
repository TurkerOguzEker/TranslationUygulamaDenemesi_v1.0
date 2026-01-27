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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import turkeroguz.eker.translationuygulamadenemesi_v10.EmailSender
import turkeroguz.eker.translationuygulamadenemesi_v10.R

class ForgotPasswordFragment : Fragment() {

    // Görünüm Grupları (LinearLayout yerine MaterialCardView kullandık)
    private lateinit var cardStep1: MaterialCardView
    private lateinit var cardStep2: MaterialCardView

    // Elemanlar
    private lateinit var etResetEmail: TextInputEditText
    private lateinit var btnSendResetCode: Button

    private lateinit var etResetCode: EditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var btnUpdatePassword: Button

    private lateinit var tvBackToLogin: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView

    private var generatedCode: String = ""
    private var userEmail: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bağlamalar
        cardStep1 = view.findViewById(R.id.cardStep1)
        cardStep2 = view.findViewById(R.id.cardStep2)

        etResetEmail = view.findViewById(R.id.etResetEmail)
        btnSendResetCode = view.findViewById(R.id.btnSendResetCode)

        etResetCode = view.findViewById(R.id.etResetCode)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword)

        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)

        // --- 1. ADIM: Kod Gönder ---
        btnSendResetCode.setOnClickListener {
            userEmail = etResetEmail.text.toString().trim()

            if (userEmail.isNotEmpty()) {
                sendVerificationCode(userEmail)
            } else {
                etResetEmail.error = "Lütfen e-posta adresinizi girin"
            }
        }

        // --- 2. ADIM: Doğrula ve Güncelle ---
        btnUpdatePassword.setOnClickListener {
            val inputCode = etResetCode.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()

            if (inputCode == generatedCode && newPass.isNotEmpty()) {
                // Kod doğru, Firebase sıfırlama linkini gönder
                sendOfficialFirebaseResetLink(userEmail)
            } else {
                Toast.makeText(context, "Hatalı Kod veya Eksik Şifre!", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun sendVerificationCode(email: String) {
        generatedCode = (100000..999999).random().toString()

        // Butonu pasif yap ki tekrar basmasın
        btnSendResetCode.isEnabled = false
        btnSendResetCode.text = "Gönderiliyor..."

        lifecycleScope.launch {
            val isSent = EmailSender.sendVerificationCode(email, "Kullanıcı", generatedCode)

            btnSendResetCode.isEnabled = true
            btnSendResetCode.text = "Doğrulama Kodu Gönder"

            if (isSent) {
                // Görünümü Değiştir
                cardStep1.visibility = View.GONE
                cardStep2.visibility = View.VISIBLE

                // Başlıkları güncelle
                tvTitle.text = "Kodu Doğrula"
                tvSubtitle.text = "$email adresine gelen kodu gir."

                Toast.makeText(context, "Kod gönderildi!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Kod gönderilemedi. E-postayı kontrol edin.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendOfficialFirebaseResetLink(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                android.app.AlertDialog.Builder(context)
                    .setTitle("İşlem Başarılı! 🎉")
                    .setMessage("Kod doğrulandı ve güvenlik kontrolü sağlandı.\n\nE-postanıza gelen 'Şifre Sıfırlama Bağlantısı'na tıklayarak yeni şifrenizi hemen belirleyebilirsiniz.")
                    .setCancelable(false)
                    .setPositiveButton("Giriş Yap") { _, _ ->
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LoginFragment())
                            .commit()
                    }
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}