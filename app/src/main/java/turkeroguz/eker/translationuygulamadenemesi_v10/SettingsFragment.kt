package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.LoginFragment

class SettingsFragment : Fragment() {

    private var btnAdminPanel: MaterialButton? = null
    private var btnLogout: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // XML'deki ID'ler ile eşleştirme
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)
        btnLogout = view.findViewById(R.id.btnLogout) // XML'e eklediğiniz yeni buton

        // Admin Kontrolü
        checkIfAdmin()

        // Admin Paneli Geçişi
        btnAdminPanel?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AdminPanelFragment())
        }

        // Çıkış Yapma İşlemi
        btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()

            // Login ekranına at ve alt menüyü gizle
            (activity as? MainActivity)?.replaceFragment(LoginFragment())
            (activity as? MainActivity)?.setBottomNavVisibility(false)
        }
    }

    private fun checkIfAdmin() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            btnAdminPanel?.visibility = View.GONE
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("role") == "admin") {
                    btnAdminPanel?.visibility = View.VISIBLE
                } else {
                    btnAdminPanel?.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                btnAdminPanel?.visibility = View.GONE
            }
    }
}