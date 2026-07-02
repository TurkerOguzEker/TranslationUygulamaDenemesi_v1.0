package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import turkeroguz.eker.translationuygulamadenemesi_v10.EmailSender
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class RegisterFragment : Fragment() {

    private lateinit var layoutRegisterForm: LinearLayout
    private lateinit var layoutVerification: LinearLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var etConfirmPass: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var tvInfoText: TextView
    private lateinit var etVerificationCode: EditText
    private lateinit var btnVerifyCode: Button
    private lateinit var btnBackToRegister: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var generatedCode: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisibility(false)

        layoutRegisterForm = view.findViewById(R.id.layoutRegisterForm)
        layoutVerification = view.findViewById(R.id.layoutVerification)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPass = view.findViewById(R.id.etPassword)
        etConfirmPass = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin)
        tvInfoText = view.findViewById(R.id.tvInfoText)
        etVerificationCode = view.findViewById(R.id.etVerificationCode)
        btnVerifyCode = view.findViewById(R.id.btnVerifyCode)
        btnBackToRegister = view.findViewById(R.id.btnBackToRegister)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val confirmPass = etConfirmPass.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                showModernMessage("⚠️ Lütfen tüm alanları doldurun.", true)
                return@setOnClickListener
            }
            if (!name.matches(Regex("^[a-zA-Z\\s]+$"))) {
                showModernMessage("⚠️ İsimde sadece İngilizce karakterler kullanılabilir!", true)
                return@setOnClickListener
            }
            if (pass != confirmPass) {
                showModernMessage("🔐 Şifreler birbiriyle uyuşmuyor!", true)
                return@setOnClickListener
            }
            if (pass.length < 6) {
                showModernMessage("🛡️ Şifre en az 6 karakter olmalıdır.", true)
                return@setOnClickListener
            }

            checkEmailAndSendCode(email, name)
        }

        btnVerifyCode.setOnClickListener {
            val inputCode = etVerificationCode.text.toString().trim()
            if (inputCode == generatedCode) {
                completeRegistration(etName.text.toString().trim(), etEmail.text.toString().trim(), etPass.text.toString().trim())
            } else {
                showModernMessage("❌ Hatalı Kod! Lütfen tekrar deneyin.", true)
            }
        }

        btnBackToRegister.setOnClickListener {
            layoutVerification.visibility = View.GONE
            layoutRegisterForm.visibility = View.VISIBLE
        }

        tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, LoginFragment()).commit()
        }
    }

    private fun checkEmailAndSendCode(email: String, name: String) {
        showModernMessage("🔍 E-posta hazırlanıyor...", false)
        btnRegister.isEnabled = false
        startVerificationProcess(email, name)
    }

    private fun startVerificationProcess(email: String, name: String) {
        generatedCode = (100000..999999).random().toString()
        showModernMessage("📩 Kod gönderiliyor...", false)
        lifecycleScope.launch {
            if (EmailSender.sendVerificationCode(email, name, generatedCode)) {
                layoutRegisterForm.visibility = View.GONE
                layoutVerification.visibility = View.VISIBLE
                tvInfoText.text = "$email adresine gelen kodu giriniz."
                showModernMessage("✅ Kod gönderildi!", false)
                btnRegister.isEnabled = true
            } else {
                showModernMessage("❌ Kod gönderilemedi! E-postayı kontrol edin.", true)
                btnRegister.isEnabled = true
            }
        }
    }

    private fun completeRegistration(name: String, email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val uid = result.user?.uid ?: ""
            // Kayıt anında lastLoginDate 0 olarak oluşuyor, ama hemen altta checkUserAndNavigate çağırınca güncellenecek.
            val newUser = User(uid = uid, email = email, name = name, registrationDate = System.currentTimeMillis())
            db.collection("users").document(uid).set(newUser).addOnSuccessListener {
                showModernMessage("🎉 Kayıt Başarılı!", false)

                // --- DÜZELTİLEN KISIM ---
                // Manuel fragment geçişi YERİNE MainActivity'yi tetikliyoruz.
                // Böylece son giriş tarihi ve seri (streak) anında güncelleniyor.
                (activity as? MainActivity)?.checkUserAndNavigate()
            }
        }.addOnFailureListener {
            if (it is FirebaseAuthUserCollisionException) showModernMessage("🚫 Bu e-posta zaten kullanımda.", true)
            else showModernMessage("❌ Kayıt başarısız: ${it.message}", true)
        }
    }

    private fun showModernMessage(message: String, isError: Boolean) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor(if (isError) "#D32F2F" else "#388E3C"))
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}