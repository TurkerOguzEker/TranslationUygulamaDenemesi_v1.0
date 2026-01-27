package turkeroguz.eker.translationuygulamadenemesi_v10

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.LoginFragment

class SettingsFragment : Fragment() {

    private lateinit var btnAdminPanel: Button
    private lateinit var btnLogout: Button
    private lateinit var cardLanguage: MaterialCardView
    private lateinit var tvCurrentLanguage: TextView
    private lateinit var btnAdminPanel: android.widget.Button
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    btnAdminPanel = view.findViewById(R.id.btnAdminPanel)

    // Admin kontrolünü başlat
    checkIfAdmin()

    // Tıklama özelliği
    btnAdminPanel.setOnClickListener {
        (activity as? MainActivity)?.replaceFragment(turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View'leri Tanımla
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)
        btnLogout = view.findViewById(R.id.btnLogout)
        cardLanguage = view.findViewById(R.id.cardLanguage)
        tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage)

        // 1. Admin Kontrolünü Başlat
        checkIfAdmin()

        // 2. Admin Butonu Tıklaması - (İSTEDİĞİN KOLAY YÖNTEM BURASI)
        btnAdminPanel.setOnClickListener {
            // ID aramadan direkt ana aktivitedeki fonksiyonu kullanır
            (activity as? MainActivity)?.replaceFragment(AdminPanelFragment())
        }

        // 3. Çıkış Yap Tıklaması
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()

            // Login ekranına yönlendir
            (activity as? MainActivity)?.replaceFragment(LoginFragment())

            // Alt menüyü gizle
            (activity as? MainActivity)?.setBottomNavVisibility(false)
        }
    }

    private fun checkIfAdmin() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists() && document.getString("role") == "admin") {
                        btnAdminPanel.visibility = android.view.View.VISIBLE
                    } else {
                        btnAdminPanel.visibility = android.view.View.GONE
                    }
                }
                .addOnFailureListener {
                    btnAdminPanel.visibility = android.view.View.GONE
                }
        } else {
            btnAdminPanel.visibility = android.view.View.GONE
        }
    }
}