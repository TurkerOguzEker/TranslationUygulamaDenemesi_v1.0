package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import turkeroguz.eker.translationuygulamadenemesi_v10.EmailSender
import turkeroguz.eker.translationuygulamadenemesi_v10.HomeFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class RegisterFragment : Fragment() {

    // Görünüm (Layout) Grupları
    private lateinit var layoutRegisterForm: LinearLayout
    private lateinit var layoutVerification: LinearLayout

    // Form Elemanları
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    // Doğrulama Elemanları
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

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kodu oluştur
            generatedCode = (100000..999999).random().toString()
            Toast.makeText(context, "Kod gönderiliyor...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                // EmailSender nesnesini kullan
                val isSent = EmailSender.sendVerificationCode(email, name, generatedCode)

                if (isSent) {
                    // Ekranı Değiştir: Formu gizle, Doğrulamayı aç
                    showVerificationScreen(email)
                } else {
                    Toast.makeText(context, "Kod gönderilemedi! E-postayı kontrol edin.", Toast.LENGTH_LONG).show()
                }
            }
        }

        // --- 2. BUTON: KODU ONAYLAMA ---
        btnVerifyCode.setOnClickListener {
            val inputCode = etVerificationCode.text.toString().trim()

            if (inputCode == generatedCode) {
                // Kod Doğru -> Kaydı Tamamla
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()

                completeRegistration(name, email, pass)
            } else {
                Toast.makeText(context, "Hatalı Kod! Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            }
        }

        // Geri Dön Butonu (E-posta yanlışsa düzeltmek için)
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

    private fun showVerificationScreen(email: String) {
        layoutRegisterForm.visibility = View.GONE
        layoutVerification.visibility = View.VISIBLE
        tvInfoText.text = "$email adresine gelen 6 haneli kodu giriniz."
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
                        Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.setBottomNavVisibility(true)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Veritabanı Hatası: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Auth Hatası: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}