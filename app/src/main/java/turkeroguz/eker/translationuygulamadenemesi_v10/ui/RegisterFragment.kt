package turkeroguz.eker.translationuygulamadenemesi_v10.ui

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
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.HomeFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class RegisterFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // NAVBAR'I GİZLE
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

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: ""

                        // Kullanıcıyı Veritabanına Kaydet
                        val newUser = User(
                            uid = uid,
                            email = email,
                            name = name,
                            registrationDate = System.currentTimeMillis()
                        )

                        db.collection("users").document(uid).set(newUser)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                // NAVBAR'I GÖSTER VE ANA SAYFAYA GİT
                                (activity as? MainActivity)?.setBottomNavVisibility(true)
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, HomeFragment())
                                    .commit()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "Lütfen gerekli alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        // Giriş Ekranına Dön
        tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}