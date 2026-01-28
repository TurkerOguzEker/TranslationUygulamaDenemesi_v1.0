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

        etResetEmail = view.findViewById(R.id.etResetEmail)
        btnSendLink = view.findViewById(R.id.btnSendLink)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        btnSendLink.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isEmpty()) {
                showModernMessage("⚠️ Lütfen e-posta adresinizi giriniz.", true)
                return@setOnClickListener
            }

            btnSendLink.isEnabled = false
            btnSendLink.text = "Gönderiliyor..."
            auth.useAppLanguage()

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    btnSendLink.isEnabled = true
                    btnSendLink.text = "Tekrar Gönder"
                    showModernMessage("✅ Bağlantı gönderildi! Spam kutusunu kontrol edin.", false)
                }
                .addOnFailureListener { exception ->
                    btnSendLink.isEnabled = true
                    btnSendLink.text = "Sıfırlama Bağlantısı Gönder"
                    if (exception is FirebaseAuthInvalidUserException) {
                        showModernMessage("🚫 Bu e-postaya kayıtlı bir kullanıcı bulunamadı.", true)
                    } else {
                        showModernMessage("❌ Hata: ${exception.localizedMessage}", true)
                    }
                }
        }

        tvBackToLogin.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun showModernMessage(message: String, isError: Boolean) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor(if (isError) "#D32F2F" else "#388E3C"))
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}