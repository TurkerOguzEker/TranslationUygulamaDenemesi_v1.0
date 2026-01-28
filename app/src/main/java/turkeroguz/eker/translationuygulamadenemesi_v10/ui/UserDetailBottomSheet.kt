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

        // Canlı Takip Başlat
        startRealtimeTracking(view)

        // Logları Getir
        setupRealtimeLogs(view.findViewById(R.id.rvUserLogs))

        view.findViewById<Button>(R.id.btnCloseSheet).setOnClickListener { dismiss() }
    }

    private fun startRealtimeTracking(view: View) {
        // Kullanıcı verisini canlı dinle
        userListener = db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val updatedUser = snapshot.toObject(User::class.java)
                    if (updatedUser != null) {
                        updateUI(view, updatedUser)
                    }
                } else {
                    // Veri henüz gelmediyse eldekiyle doldur
                    updateUI(view, user)
                }
            }
    }

    private fun updateUI(view: View, currentUser: User) {
        view.findViewById<TextView>(R.id.tvDetailName).text = currentUser.name
        view.findViewById<TextView>(R.id.tvDetailEmail).text = currentUser.email
        view.findViewById<TextView>(R.id.tvDetailUid).text = "UID: ${currentUser.uid}"

        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        // --- TARİHLERİ KONTROL ET (ID'lerin doğru olduğundan emin ol) ---
        view.findViewById<TextView>(R.id.tvDetailRegDate).text =
            if (currentUser.registrationDate > 0) dateFormat.format(Date(currentUser.registrationDate)) else "-"

        view.findViewById<TextView>(R.id.tvDetailLastLogin).text =
            if (currentUser.lastLoginDate > 0) dateFormat.format(Date(currentUser.lastLoginDate)) else "Henüz giriş yapmadı"

        view.findViewById<TextView>(R.id.tvStatWords).text = currentUser.totalWordsLearned.toString()
        view.findViewById<TextView>(R.id.tvStatRevenue).text = "₺${currentUser.totalRevenue}"
        view.findViewById<TextView>(R.id.tvStatStreak).text = currentUser.streakDays.toString()

        // --- HATA ÇÖZÜMÜ: DEĞİŞKENİ BURADA TANIMLIYORUZ ---
        val btnChangeRole = view.findViewById<Button>(R.id.btnChangeRole)

        btnChangeRole.text = currentUser.role.replaceFirstChar { it.uppercase() }

        // Artık 'btnChangeRole' değişkeni tanımlı olduğu için hata vermeyecek
        btnChangeRole.setOnClickListener { showRoleSelectionDialog(btnChangeRole) }

        val switchPremium = view.findViewById<MaterialSwitch>(R.id.switchPremiumDetail)
        val switchBan = view.findViewById<MaterialSwitch>(R.id.switchBan)

        switchPremium.setOnCheckedChangeListener(null)
        switchBan.setOnCheckedChangeListener(null)

        switchPremium.isChecked = currentUser.isPremium
        switchBan.isChecked = currentUser.isBanned

        switchPremium.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(currentUser.uid).update("isPremium", isChecked)
        }
        switchBan.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(currentUser.uid).update("isBanned", isChecked)
        }
    }

    private fun setupRealtimeLogs(rvLogs: RecyclerView) {
        rvLogs.layoutManager = LinearLayoutManager(context)

        logListener = db.collection("users").document(user.uid).collection("logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { result, e ->
                if (e != null) return@addSnapshotListener

                if (result != null && !result.isEmpty) {
                    val logs = result.toObjects(ActivityLog::class.java)
                    rvLogs.adapter = LogAdapter(logs)
                }
            }
    }

    private fun showRoleSelectionDialog(btn: Button) {
        val roles = arrayOf("user", "author", "admin")
        AlertDialog.Builder(context)
            .setTitle("Kullanıcı Rolünü Seç")
            .setItems(roles) { _, which ->
                val newRole = roles[which]
                db.collection("users").document(user.uid).update("role", newRole)
                    .addOnSuccessListener {
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