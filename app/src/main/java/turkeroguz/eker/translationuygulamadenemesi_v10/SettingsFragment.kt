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
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AuthorPanelFragment // YENİ: Yazar paneli importu

class SettingsFragment : Fragment() {

    private var btnAdminPanel: MaterialButton? = null
    private var btnAuthorPanel: MaterialButton? = null // YENİ: Yazar butonu değişkeni

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Butonları Bağla
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)
        btnAuthorPanel = view.findViewById(R.id.btnAuthorPanel) // YENİ: XML'deki butonla eşleştirme

        // 2. Uygulama Sürümünü Yazdır
        val tvVersion = view.findViewById<TextView>(R.id.txt_app_version)
        try {
            // BuildConfig dosyasından versiyon ismini çeker (Örn: 1.0)
            val versionName = turkeroguz.eker.translationuygulamadenemesi_v10.BuildConfig.VERSION_NAME
            tvVersion.text = "v$versionName"
        } catch (e: Exception) {
            tvVersion.text = "v1.0"
        }

        // 3. Yetki Kontrolünü Başlat (Hem Admin Hem Yazar kontrolü)
        checkUserRole()

        // 4. Admin Paneline Geçiş Tıklaması
        btnAdminPanel?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AdminPanelFragment())
        }

        // 5. Yazar Paneline Geçiş Tıklaması (YENİ)
        btnAuthorPanel?.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AuthorPanelFragment())
        }
    }

    private fun checkUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Eğer kullanıcı yoksa butonları gizle ve çık
        if (userId == null) {
            btnAdminPanel?.visibility = View.GONE
            btnAuthorPanel?.visibility = View.GONE
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")

                    // Önce hepsini gizle (Varsayılan durum)
                    btnAdminPanel?.visibility = View.GONE
                    btnAuthorPanel?.visibility = View.GONE

                    // Rol kontrolüne göre aç
                    if (role == "admin") {
                        btnAdminPanel?.visibility = View.VISIBLE
                        btnAuthorPanel?.visibility = View.VISIBLE // Admin her şeyi görebilir
                    } else if (role == "author") {
                        btnAuthorPanel?.visibility = View.VISIBLE // Yazar sadece kendi panelini görür
                    }
                } else {
                    btnAdminPanel?.visibility = View.GONE
                    btnAuthorPanel?.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                // Hata durumunda butonları gizle
                btnAdminPanel?.visibility = View.GONE
                btnAuthorPanel?.visibility = View.GONE
            }
    }
}