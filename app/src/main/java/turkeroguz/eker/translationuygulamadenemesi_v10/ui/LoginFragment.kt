package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class LoginFragment : Fragment() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var tvForgotPassword: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showModernMessage("❌ Google Giriş Hatası: ${e.message}", true)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisibility(false)

        etEmail = view.findViewById(R.id.etEmail)
        etPass = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister)
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword)

        // --- GİRİŞ BUTONU ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        showModernMessage("✅ Giriş Başarılı!", false)
                        goHome()
                    }
                    .addOnFailureListener { e ->
                        if (e is FirebaseAuthInvalidUserException || e is FirebaseAuthInvalidCredentialsException) {
                            showModernMessage("🚫 E-posta veya şifrenizi kontrol edin.", true)
                        } else {
                            showModernMessage("❌ Giriş başarısız: ${e.localizedMessage}", true)
                        }
                    }
            } else {
                showModernMessage("⚠️ Lütfen tüm alanları doldurun.", true)
            }
        }

        btnGoogleLogin.setOnClickListener {
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                showModernMessage("⚠️ Google hizmetleri hatası: ${e.message}", true)
            }
        }

        tvGoToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        tvForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val uid = user?.uid ?: ""

                val docRef = db.collection("users").document(uid)
                docRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val newUser = User(
                            uid = uid,
                            email = user?.email ?: "",
                            name = user?.displayName ?: "Google Kullanıcısı",
                            registrationDate = System.currentTimeMillis()
                        )
                        docRef.set(newUser).addOnSuccessListener { goHome() }
                    } else {
                        goHome()
                    }
                }
            }
            .addOnFailureListener {
                showModernMessage("❌ Kimlik doğrulama hatası: ${it.message}", true)
            }
    }

    // --- DÜZELTİLEN KISIM BURASI ---
    private fun goHome() {
        // Doğrudan Fragment değiştirmek yerine MainActivity'deki kontrol fonksiyonunu çağırıyoruz.
        // Bu fonksiyon hem yönlendirme yapar hem de son giriş tarihini günceller.
        (activity as? MainActivity)?.checkUserAndNavigate()
    }

    private fun showModernMessage(message: String, isError: Boolean) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(Color.parseColor("#D32F2F"))
            snackbar.setTextColor(Color.WHITE)
        } else {
            snackbar.setBackgroundTint(Color.parseColor("#388E3C"))
            snackbar.setTextColor(Color.WHITE)
        }
        snackbar.show()
    }
}