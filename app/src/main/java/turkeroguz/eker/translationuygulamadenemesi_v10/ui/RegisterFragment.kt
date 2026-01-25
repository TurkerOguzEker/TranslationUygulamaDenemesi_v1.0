package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException // YENİ EKLENEN IMPORT
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentRegisterBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import turkeroguz.eker.translationuygulamadenemesi_v10.R
// LoginFragment aynı pakette olduğu için import gerekmeyebilir, gerekirse otomatik eklenir.

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegisterAction.setOnClickListener {
            val email = binding.etRegEmail.text.toString().trim()
            val pass = binding.etRegPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Kayıt işlemini başlat
                auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->

                    // Kullanıcı veritabanı nesnesini oluştur
                    val newUser = User(uid = result.user!!.uid, email = email, role = "user")

                    // Firestore'a kaydet
                    db.collection("users").document(result.user!!.uid).set(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Kayıt Başarılı! Lütfen giriş yapınız.", Toast.LENGTH_SHORT).show()

                            auth.signOut() // Oturumu kapat (Giriş ekranına temiz gitmek için)

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, LoginFragment())
                                .commit()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Veritabanı hatası: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                }.addOnFailureListener { exception ->
                    // BURASI GÜNCELLENDİ: Hata türüne göre mesaj veriyoruz
                    if (exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(context, "Bu hesapla daha önceden kayıt olunmuş!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Kayıt Hatası: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Lütfen e-posta ve şifre giriniz.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}