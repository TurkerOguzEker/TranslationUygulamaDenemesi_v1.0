package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // HATA VEREN SATIR SİLİNDİ (val layout = ...)
        // Artık butonu XML'den kontrol ediyoruz.

        checkIfAdmin(view)

        return view
    }

    private fun checkIfAdmin(view: View) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    // Eğer kullanıcı "admin" ise butonu göster
                    if (document.exists() && document.getString("role") == "admin") {
                        setupAdminButton(view)
                    }
                }
        }
    }

    private fun setupAdminButton(view: View) {
        // XML'e eklediğimiz butonu bul
        val btnAdmin = view.findViewById<Button>(R.id.btnAdminPanel)

        if (btnAdmin != null) {
            btnAdmin.visibility = View.VISIBLE // Görünür yap
            btnAdmin.setOnClickListener {
                // Admin paneline git
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AdminPanelFragment())
                    .addToBackStack(null) // Geri tuşuyla ayarlara dönmeyi sağlar
                    .commit()
            }
        }
    }
}