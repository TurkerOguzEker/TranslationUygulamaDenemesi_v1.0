package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment

class SettingsFragment : Fragment() {

    private var btnAdminPanel: MaterialButton? = null
    // NOT: btnAuthorPanel tamamen kaldırıldı.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Sadece Admin Butonunu Bağla
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)

        // 2. Uygulama Sürümünü Yazdır
        val tvVersion = view.findViewById<TextView>(R.id.txt_app_version)
        try {
            // BuildConfig dosyasından versiyon ismini çeker (Örn: 1.0)
            val versionName = turkeroguz.eker.translationuygulamadenemesi_v10.BuildConfig.VERSION_NAME
            tvVersion.text = "v$versionName"
        } catch (e: Exception) {
            tvVersion.text = "v1.0"
        }

        // 3. Yetki Kontrolünü Başlat (Sadece Admin için)
        checkUserRole()

        // 4. Admin Paneline Geçiş Tıklaması
        btnAdminPanel?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AdminPanelFragment())
        }
    }

    private fun checkUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Eğer kullanıcı yoksa butonu gizle ve çık
        if (userId == null) {
            btnAdminPanel?.visibility = View.GONE
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")

                    // Rol kontrolüne göre sadece Admin butonunu aç
                    if (role == "admin") {
                        btnAdminPanel?.visibility = View.VISIBLE
                    } else {
                        btnAdminPanel?.visibility = View.GONE
                    }
                } else {
                    btnAdminPanel?.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                // Hata durumunda butonu gizle
                btnAdminPanel?.visibility = View.GONE
            }
    }
}