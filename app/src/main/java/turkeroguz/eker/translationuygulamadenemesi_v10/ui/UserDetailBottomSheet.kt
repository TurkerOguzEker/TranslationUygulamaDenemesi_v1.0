package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.LogAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.ActivityLog
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import java.text.SimpleDateFormat
import java.util.*

class UserDetailBottomSheet(private val user: User) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private var logListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_user_detail_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verileri Canlı İzlemeye Başla
        startRealtimeTracking(view)

        // Log Listesini Hazırla
        setupRealtimeLogs(view.findViewById(R.id.rvUserLogs))

        view.findViewById<Button>(R.id.btnCloseSheet).setOnClickListener { dismiss() }
    }

    private fun startRealtimeTracking(view: View) {
        userListener = db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val updatedUser = snapshot.toObject(User::class.java)
                    if (updatedUser != null) {
                        updateUI(view, updatedUser)
                    }
                } else {
                    updateUI(view, user)
                }
            }
    }

    private fun updateUI(view: View, currentUser: User) {
        // --- TEMEL BİLGİLER ---
        view.findViewById<TextView>(R.id.tvDetailName).text = currentUser.name
        view.findViewById<TextView>(R.id.tvDetailEmail).text = currentUser.email
        view.findViewById<TextView>(R.id.tvDetailUid).text = "UID: ${currentUser.uid}"

        // --- TARİH FORMATLAMA ---
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        // 1. Kayıt Tarihi
        view.findViewById<TextView>(R.id.tvDetailRegDate).text =
            if (currentUser.registrationDate > 0) dateFormat.format(Date(currentUser.registrationDate)) else "-"

        // 2. Son Giriş Tarihi
        view.findViewById<TextView>(R.id.tvDetailLastLogin).text =
            if (currentUser.lastLoginDate > 0) dateFormat.format(Date(currentUser.lastLoginDate)) else "Henüz giriş yapmadı"

        // --- İSTATİSTİKLER ---
        // Kelime Sayısı
        view.findViewById<TextView>(R.id.tvStatWords).text = currentUser.totalWordsLearned.toString()
        // Harcama (Revenue)
        view.findViewById<TextView>(R.id.tvStatRevenue).text = "₺${currentUser.totalRevenue}"
        // Okunan Kitap Sayısı (Streak yerine bunu göstermek daha mantıklı olabilir veya ikisi de)
        view.findViewById<TextView>(R.id.tvStatStreak).text = "${currentUser.storiesCompleted} Kitap"


        // --- YETKİ VE BUTONLAR ---
        val btnChangeRole = view.findViewById<Button>(R.id.btnChangeRole)
        btnChangeRole.text = currentUser.role.replaceFirstChar { it.uppercase() }
        btnChangeRole.setOnClickListener { showRoleSelectionDialog(btnChangeRole) }

        val switchPremium = view.findViewById<MaterialSwitch>(R.id.switchPremiumDetail)
        val switchBan = view.findViewById<MaterialSwitch>(R.id.switchBan)

        // Dinleyicileri geçici olarak kaldır (Sonsuz döngüyü önlemek için)
        switchPremium.setOnCheckedChangeListener(null)
        switchBan.setOnCheckedChangeListener(null)

        switchPremium.isChecked = currentUser.isPremium
        switchBan.isChecked = currentUser.isBanned

        // --- PREMIUM SWITCH İŞLEMİ VE LOGLAMA ---
        switchPremium.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(currentUser.uid).update("isPremium", isChecked)
                .addOnSuccessListener {
                    val status = if (isChecked) "Premium Yapıldı" else "Premium İptal"
                    logAdminAction(currentUser.uid, "YÖNETİCİ İŞLEMİ", "Kullanıcı $status.", "warning")
                }
        }

        // --- BAN SWITCH İŞLEMİ VE LOGLAMA ---
        switchBan.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(currentUser.uid).update("isBanned", isChecked)
                .addOnSuccessListener {
                    val status = if (isChecked) "Yasaklandı (BAN)" else "Yasağı Kaldırıldı"
                    logAdminAction(currentUser.uid, "YÖNETİCİ İŞLEMİ", "Kullanıcı $status.", "error")
                }
        }
    }

    // --- LOG LİSTESİNİ ÇEKME ---
    private fun setupRealtimeLogs(rvLogs: RecyclerView) {
        rvLogs.layoutManager = LinearLayoutManager(context)

        logListener = db.collection("users").document(user.uid).collection("logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50) // Son 50 işlemi göster
            .addSnapshotListener { result, e ->
                if (e != null) return@addSnapshotListener

                if (result != null && !result.isEmpty) {
                    val logs = result.toObjects(ActivityLog::class.java)
                    rvLogs.adapter = LogAdapter(logs)
                }
            }
    }

    // --- ADMİN İŞLEMLERİNİ LOGA KAYDETME FONKSİYONU ---
    private fun logAdminAction(uid: String, action: String, details: String, type: String) {
        val log = hashMapOf(
            "action" to action,
            "details" to details,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to type
        )
        db.collection("users").document(uid).collection("logs").add(log)
    }

    private fun showRoleSelectionDialog(btn: Button) {
        val roles = arrayOf("user", "author", "admin")
        AlertDialog.Builder(context)
            .setTitle("Kullanıcı Rolünü Seç")
            .setItems(roles) { _, which ->
                val newRole = roles[which]
                db.collection("users").document(user.uid).update("role", newRole)
                    .addOnSuccessListener {
                        logAdminAction(user.uid, "ROL DEĞİŞTİ", "Yeni rol: ${newRole.uppercase()}", "warning")
                        Toast.makeText(context, "Rol: $newRole yapıldı", Toast.LENGTH_SHORT).show()
                        btn.text = newRole.uppercase()
                    }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logListener?.remove()
        userListener?.remove()
    }
}