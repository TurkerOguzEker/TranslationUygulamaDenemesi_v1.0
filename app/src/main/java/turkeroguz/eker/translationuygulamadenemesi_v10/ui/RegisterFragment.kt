package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import turkeroguz.eker.translationuygulamadenemesi_v10.HomeFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import turkeroguz.eker.translationuygulamadenemesi_v10.EmailSender
// Yukarıdaki satır doğru olan. '.util' içeren hatalı satır kaldırıldı.

class RegisterFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Doğrulama kodu için geçici değişken
    private var generatedCode: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisibility(false)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPass = view.findViewById(R.id.etPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. KOD OLUŞTUR VE MAİL GÖNDER
            generatedCode = (100000..999999).random().toString()

            // Kullanıcıya bilgi ver
            Toast.makeText(context, "Doğrulama kodu gönderiliyor...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                val isSent = EmailSender.sendEmail(
                    email,
                    "Doğrulama Kodu",
                    "Merhaba $name,\n\nUygulamaya kayıt kodun: $generatedCode"
                )

                if (isSent) {
                    showVerificationDialog(name, email, pass)
                } else {
                    Toast.makeText(context, "Kod gönderilemedi! E-postayı kontrol edin.", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun showVerificationDialog(name: String, email: String, pass: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_verification_code, null)
        val etCode = dialogView.findViewById<EditText>(R.id.etVerificationCode)

        AlertDialog.Builder(context)
            .setTitle("E-posta Doğrulama")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Doğrula") { dialog, _ ->
                val inputCode = etCode.text.toString().trim()
                if (inputCode == generatedCode) {
                    // KOD DOĞRU -> FIREBASE KAYDI BAŞLASIN
                    registerUserToFirebase(name, email, pass)
                } else {
                    Toast.makeText(context, "Hatalı Kod!", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun registerUserToFirebase(name: String, email: String, pass: String) {
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