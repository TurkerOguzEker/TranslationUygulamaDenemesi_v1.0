package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserDetailBottomSheet(private val user: User) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var btnChangeRole: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_user_detail_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View Bağlamaları
        view.findViewById<TextView>(R.id.tvDetailName).text = user.name
        view.findViewById<TextView>(R.id.tvDetailEmail).text = user.email
        view.findViewById<TextView>(R.id.tvDetailUid).text = "UID: ${user.uid}"

        // Rol Değiştirme Butonu
        btnChangeRole = view.findViewById(R.id.btnChangeRole)
        btnChangeRole.text = user.role.replaceFirstChar { it.uppercase() } // Mevcut rolü yaz

        btnChangeRole.setOnClickListener {
            showRoleSelectionDialog()
        }

        // Switchler
        val switchPremium = view.findViewById<MaterialSwitch>(R.id.switchPremiumDetail)
        val switchBan = view.findViewById<MaterialSwitch>(R.id.switchBan)

        switchPremium.isChecked = user.isPremium
        switchBan.isChecked = user.isBanned

        // Premium Güncelleme
        switchPremium.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(user.uid).update("isPremium", isChecked)
        }

        // Ban Güncelleme
        switchBan.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(user.uid).update("isBanned", isChecked)
        }

        view.findViewById<Button>(R.id.btnCloseSheet).setOnClickListener { dismiss() }
    }

    private fun showRoleSelectionDialog() {
        val roles = arrayOf("user", "author", "admin")
        val currentRoleIndex = roles.indexOf(user.role.lowercase())
        val checkedItem = if (currentRoleIndex >= 0) currentRoleIndex else 0

        AlertDialog.Builder(context)
            .setTitle("Kullanıcı Rolünü Seç")
            .setSingleChoiceItems(roles, checkedItem) { dialog, which ->
                val selectedRole = roles[which]
                updateUserRole(selectedRole)
                dialog.dismiss()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updateUserRole(newRole: String) {
        if (user.uid.isEmpty()) return

        db.collection("users").document(user.uid).update("role", newRole)
            .addOnSuccessListener {
                // Başarılı olursa buton metnini güncelle
                btnChangeRole.text = newRole.replaceFirstChar { it.uppercase() }
                Toast.makeText(context, "Rol güncellendi: $newRole", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}