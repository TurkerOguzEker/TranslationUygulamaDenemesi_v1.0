package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentLoginBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.RegisterFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.R

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLoginAction.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val pass = binding.etLoginPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener {
                    Toast.makeText(context, "Giriş Başarılı", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).checkUserAndNavigate()
                }.addOnFailureListener {
                    Toast.makeText(context, "Giriş Hatası: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .commit()
        }
    }
}