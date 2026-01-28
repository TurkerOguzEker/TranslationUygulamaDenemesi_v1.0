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
import turkeroguz.eker.translationuygulamadenemesi_v10.HomeFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class RegisterFragment : Fragment() {

    private lateinit var layoutRegisterForm: LinearLayout
    private lateinit var layoutVerification: LinearLayout

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var etConfirmPass: TextInputEditText // İkinci şifre kutusu
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

        // --- View Bağlamaları ---
        layoutRegisterForm = view.findViewById(R.id.layoutRegisterForm)
        layoutVerification = view.findViewById(R.id.layoutVerification)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPass = view.findViewById(R.id.etPassword)
        etConfirmPass = view.findViewById(R.id.etConfirmPassword) // XML'de bu ID'nin olduğundan emin olun
        btnRegister = view.findViewById(R.id.btnRegister)
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin)

        tvInfoText = view.findViewById(R.id.tvInfoText)
        etVerificationCode = view.findViewById(R.id.etVerificationCode)
        btnVerifyCode = view.findViewById(R.id.btnVerifyCode)
        btnBackToRegister = view.findViewById(R.id.btnBackToRegister)

        // --- 1. BUTON: KOD GÖNDERME ---
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val confirmPass = etConfirmPass.text.toString().trim()

            // 1. Validasyonlar
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                showModernMessage("⚠️ Lütfen tüm alanları doldurun.", isError = true)
                return@setOnClickListener
            }

            // İngilizce Karakter Kontrolü
            val englishCharRegex = Regex("^[a-zA-Z\\s]+$")
            if (!name.matches(englishCharRegex)) {
                showModernMessage("⚠️ İsimde sadece İngilizce karakterler kullanılabilir!", isError = true)
                return@setOnClickListener
            }

            // Şifre Eşleşme Kontrolü
            if (pass != confirmPass) {
                showModernMessage("🔐 Şifreler birbiriyle uyuşmuyor!", isError = true)
                return@setOnClickListener
            }

            if (pass.length < 6) {
                showModernMessage("🛡️ Şifre en az 6 karakter olmalıdır.", isError = true)
                return@setOnClickListener
            }

            // 2. E-POSTA KONTROLÜ VE KOD GÖNDERME
            // Kod göndermeden önce mailin kayıtlı olup olmadığına bakıyoruz
            checkEmailAndSendCode(email, name)
        }

        // --- 2. BUTON: KODU ONAYLAMA ---
        btnVerifyCode.setOnClickListener {
            val inputCode = etVerificationCode.text.toString().trim()
            if (inputCode == generatedCode) {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()
                completeRegistration(name, email, pass)
            } else {
                showModernMessage("❌ Hatalı Kod! Lütfen tekrar deneyin.", isError = true)
            }
        }

        btnBackToRegister.setOnClickListener {
            layoutVerification.visibility = View.GONE
            layoutRegisterForm.visibility = View.VISIBLE
        }

        tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    // --- E-POSTA KONTROL FONKSİYONU ---
    private fun checkEmailAndSendCode(email: String, name: String) {
        showModernMessage("🔍 E-posta kontrol ediliyor...", isError = false)
        btnRegister.isEnabled = false // Çift tıklamayı önle

        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                btnRegister.isEnabled = true

                // Eğer liste boş değilse, bu e-posta kayıtlı demektir
                val methods = result.signInMethods
                if (methods != null && methods.isNotEmpty()) {
                    showModernMessage("🚫 Bu e-posta zaten kullanımda! Giriş yapın.", isError = true)
                } else {
                    // E-posta temiz, kod gönderme işlemine başla
                    startVerificationProcess(email, name)
                }
            }
            .addOnFailureListener {
                btnRegister.isEnabled = true
                // Hata durumunda (internet yoksa vb.) uyarı ver
                showModernMessage("⚠️ Bağlantı hatası: ${it.message}", isError = true)
            }
    }

    private fun startVerificationProcess(email: String, name: String) {
        generatedCode = (100000..999999).random().toString()
        showModernMessage("📩 Kod gönderiliyor, lütfen bekleyin...", isError = false)

        lifecycleScope.launch {
            val isSent = EmailSender.sendVerificationCode(email, name, generatedCode)
            if (isSent) {
                layoutRegisterForm.visibility = View.GONE
                layoutVerification.visibility = View.VISIBLE
                tvInfoText.text = "$email adresine gelen 6 haneli kodu giriniz."
                showModernMessage("✅ Kod gönderildi!", isError = false)
            } else {
                showModernMessage("❌ Kod gönderilemedi! E-posta adresini kontrol edin.", isError = true)
            }
        }
    }

    private fun completeRegistration(name: String, email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val newUser = User(
                    uid = uid,
                    email = email,
                    name = name,
                    registrationDate = System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener {
                        showModernMessage("🎉 Kayıt Başarılı! Hoş geldiniz.", isError = false)
                        (activity as? MainActivity)?.setBottomNavVisibility(true)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                    .addOnFailureListener {
                        showModernMessage("❌ Veritabanı Hatası: ${it.message}", isError = true)
                    }
            }
            .addOnFailureListener { exception ->
                if (exception is FirebaseAuthUserCollisionException) {
                    showModernMessage("🚫 Bu e-posta zaten bir hesaba bağlı.", isError = true)
                } else {
                    showModernMessage("❌ Kayıt başarısız: ${exception.message}", isError = true)
                }
            }
    }

    // Modern Renkli Bildirim (Snackbar)
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