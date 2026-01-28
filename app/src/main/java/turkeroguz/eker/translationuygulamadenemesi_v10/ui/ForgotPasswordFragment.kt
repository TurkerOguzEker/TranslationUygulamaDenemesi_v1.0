package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R

class ForgotPasswordFragment : Fragment() {

    // XML'deki ID'lerle tam uyumlu değişkenler
    private lateinit var etResetEmail: TextInputEditText
    private lateinit var btnSendLink: Button
    private lateinit var tvBackToLogin: TextView

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisibility(false)

        // 1. XML'deki Doğru ID'lerle Eşleştirme (Düzeltildi)
        etResetEmail = view.findViewById(R.id.etResetEmail)
        btnSendLink = view.findViewById(R.id.btnSendLink)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        // 2. Gönder Butonu İşlemi
        btnSendLink.setOnClickListener {
            val email = etResetEmail.text.toString().trim()

            if (email.isEmpty()) {
                showModernMessage("⚠️ Lütfen e-posta adresinizi giriniz.", isError = true)
                return@setOnClickListener
            }

            // Butonu geçici olarak kilitle (Çift tıklamayı önler)
            btnSendLink.isEnabled = false
            btnSendLink.text = "Gönderiliyor..."

            auth.useAppLanguage() // E-postanın telefonun dilinde gitmesini sağlar

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    btnSendLink.isEnabled = true
                    btnSendLink.text = "Tekrar Gönder"

                    showModernMessage("✅ Bağlantı gönderildi! Spam klasörünü kontrol edin.", isError = false)
                }
                .addOnFailureListener { exception ->
                    btnSendLink.isEnabled = true
                    btnSendLink.text = "Sıfırlama Bağlantısı Gönder"

                    // --- ÖZEL HATA YAKALAMA ---
                    if (exception is FirebaseAuthInvalidUserException) {
                        // Kayıtlı olmayan mail hatası
                        showModernMessage("🚫 Bu e-postaya kayıtlı bir kullanıcı bulunamadı.", isError = true)
                    } else {
                        // Diğer hatalar
                        showModernMessage("❌ Hata: ${exception.localizedMessage}", isError = true)
                    }
                }
        }

        // 3. Geri Dön Butonu
        tvBackToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // Modern Renkli Bildirim Gösterme Fonksiyonu
    private fun showModernMessage(message: String, isError: Boolean) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(Color.parseColor("#D32F2F")) // Kırmızı
            snackbar.setTextColor(Color.WHITE)
        } else {
            snackbar.setBackgroundTint(Color.parseColor("#388E3C")) // Yeşil
            snackbar.setTextColor(Color.WHITE)
        }
        snackbar.show()
    }
}