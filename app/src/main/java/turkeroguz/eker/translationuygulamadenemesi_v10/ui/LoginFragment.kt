package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.HomeFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.MainActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class LoginFragment : Fragment() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tvGoToRegister: TextView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Google Giriş Sonucunu Yakalayan Yapı
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Giriş Hatası: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // Normal Giriş
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString().trim()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        goHome()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "Lütfen alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Giriş Butonu
        btnGoogleLogin.setOnClickListener {
            // ÖNEMLİ: string.xml içinde 'default_web_client_id' otomatik oluşur ama
            // google-services.json dosyanızın güncel olması gerekir.
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        tvGoToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                // İlk defa giriyorsa veritabanına kaydet
                val user = authResult.user
                val uid = user?.uid ?: ""
                val email = user?.email ?: ""
                val name = user?.displayName ?: "Google Kullanıcısı"

                val docRef = db.collection("users").document(uid)
                docRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val newUser = User(
                            uid = uid,
                            email = email,
                            name = name,
                            registrationDate = System.currentTimeMillis()
                        )
                        docRef.set(newUser).addOnSuccessListener { goHome() }
                    } else {
                        goHome()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Auth Hatası: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goHome() {
        Toast.makeText(context, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.setBottomNavVisibility(true)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }
}