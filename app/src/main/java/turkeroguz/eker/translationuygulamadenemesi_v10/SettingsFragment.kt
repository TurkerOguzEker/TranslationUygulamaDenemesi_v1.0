package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment

class SettingsFragment : Fragment() {

    // Sadece Admin butonu tanımlı, çıkış butonu kaldırıldı
    private var btnAdminPanel: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Admin Butonunu Bağla
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)

        // 2. Admin Yetki Kontrolünü Başlat
        checkIfAdmin()

        // 3. Admin Paneline Geçiş Tıklaması
        btnAdminPanel?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AdminPanelFragment())
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
                if (document.exists()) {
                    // Veritabanından gelen değeri logla
                    val role = document.getString("role")
                    android.util.Log.d("ADMIN_KONTROL", "Kullanıcı ID: $userId")
                    android.util.Log.d("ADMIN_KONTROL", "Gelen Role Değeri: $role")

                    if (role == "admin") {
                        android.util.Log.d("ADMIN_KONTROL", "Eşleşme BAŞARILI. Buton gösteriliyor.")
                        btnAdminPanel?.visibility = View.VISIBLE
                    } else {
                        android.util.Log.d("ADMIN_KONTROL", "Eşleşme BAŞARISIZ. Beklenen: 'admin', Gelen: '$role'")
                        btnAdminPanel?.visibility = View.GONE
                    }
                } else {
                    android.util.Log.d("ADMIN_KONTROL", "Kullanıcı dökümanı bulunamadı!")
                    btnAdminPanel?.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ADMIN_KONTROL", "Firebase Hatası: ${e.localizedMessage}")
                btnAdminPanel?.visibility = View.GONE
            }
    }

}