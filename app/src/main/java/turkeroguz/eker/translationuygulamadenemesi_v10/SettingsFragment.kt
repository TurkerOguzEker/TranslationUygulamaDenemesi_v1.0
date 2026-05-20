package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment
import java.io.File

class SettingsFragment : Fragment() {

    private var btnAdminPanel: MaterialButton? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val googleReAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                auth.currentUser?.reauthenticate(credential)
                    ?.addOnSuccessListener { deleteFirestoreDataAndAuthAccount() }
                    ?.addOnFailureListener {
                        Toast.makeText(context, "Google doğrulama başarısız.", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google doğrulama hatası.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)

        val tvVersion = view.findViewById<TextView>(R.id.txt_app_version)
        try {
            tvVersion.text = "v${BuildConfig.VERSION_NAME}"
        } catch (e: Exception) {
            tvVersion.text = "v1.0"
        }

        checkUserRole()

        btnAdminPanel?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AdminPanelFragment())
        }

        view.findViewById<View>(R.id.btn_delete_account)?.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showDeleteAccountConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hesabı Sil")
            .setMessage(
                "Hesabınız ve tüm verileriniz (kelimeler, okuma geçmişi, favoriler) kalıcı olarak silinecek.\n\n" +
                "Bu işlem geri alınamaz!"
            )
            .setPositiveButton("Devam Et") { _, _ -> startDeletionFlow() }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun startDeletionFlow() {
        val user = auth.currentUser ?: return
        val isEmailUser = user.providerData.any { it.providerId == "password" }

        if (isEmailUser) {
            showPasswordConfirmDialog(user.email ?: "")
        } else {
            // Google kullanıcısı: Google ile yeniden doğrula
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleReAuthLauncher.launch(GoogleSignIn.getClient(requireActivity(), gso).signInIntent)
        }
    }

    private fun showPasswordConfirmDialog(email: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete_account, null)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Şifreni Doğrula")
            .setMessage("Hesabını kalıcı olarak silmek için şifreni gir.")
            .setView(dialogView)
            .setPositiveButton("Hesabı Sil") { _, _ ->
                val password = etPassword?.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(context, "Şifre boş olamaz.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val credential = EmailAuthProvider.getCredential(email, password)
                auth.currentUser?.reauthenticate(credential)
                    ?.addOnSuccessListener { deleteFirestoreDataAndAuthAccount() }
                    ?.addOnFailureListener {
                        Toast.makeText(context, "Yanlış şifre, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteFirestoreDataAndAuthAccount() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val subcollections = listOf("words", "book_progress", "favorites", "finished_books", "logs")
        var completedCount = 0

        fun onSubcollectionDone() {
            completedCount++
            if (completedCount < subcollections.size) return

            // Tüm alt koleksiyonlar silindi, ana dökümanı sil
            db.collection("users").document(uid).delete().addOnCompleteListener {
                // Firebase Auth kaydını sil
                user.delete()
                    .addOnSuccessListener {
                        clearLocalBooks()
                        Toast.makeText(context, "Hesabınız başarıyla silindi.", Toast.LENGTH_LONG).show()
                        (activity as? MainActivity)?.checkUserAndNavigate()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        for (collection in subcollections) {
            db.collection("users").document(uid).collection(collection).get()
                .addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        onSubcollectionDone()
                        return@addOnSuccessListener
                    }
                    val batch = db.batch()
                    docs.forEach { batch.delete(it.reference) }
                    batch.commit().addOnCompleteListener { onSubcollectionDone() }
                }
                .addOnFailureListener { onSubcollectionDone() }
        }
    }

    private fun clearLocalBooks() {
        try {
            File(requireContext().filesDir, "downloaded_books").deleteRecursively()
        } catch (e: Exception) { /* ignore */ }
    }

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            btnAdminPanel?.visibility = View.GONE
            return
        }
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = if (document.exists()) document.getString("role") else null
                btnAdminPanel?.visibility = if (role == "admin") View.VISIBLE else View.GONE
            }
            .addOnFailureListener { btnAdminPanel?.visibility = View.GONE }
    }
}
