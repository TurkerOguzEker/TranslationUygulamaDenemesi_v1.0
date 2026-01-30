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

        // 2. Uygulama Sürümünü Yazdır (DÜZELTİLEN KISIM)
        val tvVersion = view.findViewById<TextView>(R.id.txt_app_version)
        try {
            // BuildConfig dosyasından versiyon ismini çeker (Örn: 1.0)
            val versionName = turkeroguz.eker.translationuygulamadenemesi_v10.BuildConfig.VERSION_NAME
            tvVersion.text = "v$versionName"
        } catch (e: Exception) {
            tvVersion.text = "v1.0"
        }

        // 3. Admin Yetki Kontrolünü Başlat
        checkIfAdmin()

        // 4. Admin Paneline Geçiş Tıklaması
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
                    val role = document.getString("role")
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
                btnAdminPanel?.visibility = View.GONE
            }
    }
}