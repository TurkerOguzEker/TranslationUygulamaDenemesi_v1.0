package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.LayoutUserDetailSheetBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.ActivityLog
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import java.text.SimpleDateFormat
import java.util.*

class UserDetailBottomSheet(private val user: User) : BottomSheetDialogFragment() {

    private var _binding: LayoutUserDetailSheetBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutUserDetailSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- İSİM MANTIĞINI DÜZELTEN KISIM ---
        val displayName = if (user.name.isNotEmpty()) {
            user.name
        } else {
            // İsmi yoksa e-posta adresinin @ işaretinden önceki kısmını al
            user.email.substringBefore("@")
        }

        binding.tvDetailName.text = displayName
        // -------------------------------------

        binding.tvDetailEmail.text = user.email
        binding.tvDetailUid.text = "UID: ${user.uid}"
        binding.tvDevice.text = user.deviceInfo

        // Tarih Formatlama
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
        binding.tvRegDate.text = if (user.registrationDate > 0) dateFormat.format(Date(user.registrationDate)) else "Bilinmiyor"
        binding.tvLastLogin.text = if (user.lastLoginDate > 0) dateFormat.format(Date(user.lastLoginDate)) else "Hiç girmedi"

        // --- İSTATİSTİKLERİ DOLDUR ---
        binding.tvStatWords.text = user.totalWordsLearned.toString()
        binding.tvStatRevenue.text = "₺${user.totalRevenue}"
        binding.tvStatStreak.text = user.streakDays.toString()

        // --- BUTON VE SWITCH DURUMLARI ---

        // 1. ROL AYARI
        binding.btnChangeRole.text = user.role.uppercase()
        binding.btnChangeRole.setOnClickListener { showRoleSelectionDialog() }

        // 2. PREMIUM SWITCH
        binding.switchPremium.setOnCheckedChangeListener(null) // Listener çakışmasını önle
        binding.switchPremium.isChecked = user.isPremium
        binding.switchPremium.setOnCheckedChangeListener { _, isChecked ->
            updateUserField("isPremium", isChecked, "Premium ${if(isChecked) "Verildi" else "Alındı"}")
        }

        // 3. BAN SWITCH
        binding.switchBan.setOnCheckedChangeListener(null) // Listener çakışmasını önle
        binding.switchBan.isChecked = user.isBanned
        binding.switchBan.setOnCheckedChangeListener { _, isChecked ->
            val action = if(isChecked) "Kullanıcı BANLANDI" else "Ban Kaldırıldı"
            updateUserField("isBanned", isChecked, action)
        }

        // 4. ŞİFRE SIFIRLAMA
        binding.btnResetPassword.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.email)
                .addOnSuccessListener { Toast.makeText(context, "Sıfırlama maili gönderildi", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show() }
        }

        // 5. HESAP SİLME (Soft Delete)
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hesabı Sil")
                .setMessage("Bu işlem geri alınamaz. Kullanıcı silinmiş olarak işaretlenecek.")
                .setPositiveButton("Sil") { _, _ ->
                    updateUserField("isDeleted", true, "Hesap Silindi (Soft Delete)")
                    dismiss()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // 6. LOGLARI ÇEK
        fetchLogs()
    }

    private fun showRoleSelectionDialog() {
        val roles = arrayOf("user", "author", "admin")
        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Rol Seçin")
            .setItems(roles) { _, which ->
                val newRole = roles[which]
                updateUserField("role", newRole, "Rol değişti: $newRole")
                binding.btnChangeRole.text = newRole.uppercase()
            }
            .show()
    }

    private fun updateUserField(field: String, value: Any, logMessage: String) {
        db.collection("users").document(user.uid)
            .update(field, value)
            .addOnSuccessListener {
                Toast.makeText(context, "Güncellendi", Toast.LENGTH_SHORT).show()
                logAdminAction(logMessage)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logAdminAction(desc: String) {
        val log = ActivityLog(
            userId = user.uid,
            actionType = "ADMIN_ACTION",
            description = desc,
            timestamp = System.currentTimeMillis()
        )
        db.collection("activity_logs").add(log)
    }

    // Logları listelemek için fonksiyon (Eksikse ekleyin)
    private fun fetchLogs() {
        // Eğer layout dosyanızda rvDetailLogs varsa bu kodu açabilirsiniz:
        /*
        db.collection("activity_logs")
            .whereEqualTo("userId", user.uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val logs = result.toObjects(ActivityLog::class.java)
                binding.rvDetailLogs.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                binding.rvDetailLogs.adapter = turkeroguz.eker.translationuygulamadenemesi_v10.adapter.LogAdapter(logs)
            }
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}