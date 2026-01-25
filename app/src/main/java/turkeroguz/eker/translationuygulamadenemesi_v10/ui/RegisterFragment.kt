package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentRegisterBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R

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
            val email = binding.etRegEmail.text.toString()
            val pass = binding.etRegPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
                    // Auth başarılı, Firestore'a kullanıcıyı ekle
                    val newUser = User(uid = result.user!!.uid, email = email, role = "user")

                    db.collection("users").document(result.user!!.uid).set(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                            // Ana sayfaya yönlendir
                            (activity as MainActivity).checkUserAndNavigate()
                        }
                }.addOnFailureListener {
                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}