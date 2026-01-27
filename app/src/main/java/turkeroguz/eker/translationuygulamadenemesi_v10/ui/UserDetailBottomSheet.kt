package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDetailBottomSheet(private val user: User) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_user_detail_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View Tanımlamaları
        val tvName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvEmail = view.findViewById<TextView>(R.id.tvDetailEmail)
        val tvUid = view.findViewById<TextView>(R.id.tvDetailUid)
        val ivIcon = view.findViewById<ImageView>(R.id.ivSheetIcon)

        // İstatistikler
        val tvWords = view.findViewById<TextView>(R.id.tvStatWords)
        val tvRevenue = view.findViewById<TextView>(R.id.tvStatRevenue)
        val tvStreak = view.findViewById<TextView>(R.id.tvStatStreak)

        // Teknik Detaylar
        val tvRegDate = view.findViewById<TextView>(R.id.tvRegDate)
        val tvLastLogin = view.findViewById<TextView>(R.id.tvLastLogin)
        val tvStoriesCount = view.findViewById<TextView>(R.id.tvStoriesCount)

        // Butonlar ve Switchler
        val btnRole = view.findViewById<Button>(R.id.btnChangeRole)
        val switchPremium = view.findViewById<MaterialSwitch>(R.id.switchPremiumDetail)
        val switchBan = view.findViewById<MaterialSwitch>(R.id.switchBan)
        val btnResetPass = view.findViewById<Button>(R.id.btnResetPassword)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteAccount)
        val btnClose = view.findViewById<Button>(R.id.btnCloseSheet)

        // --- VERİLERİ DOLDUR ---
        tvName.text = user.name ?: "İsimsiz Kullanıcı"
        tvEmail.text = user.email
        tvUid.text = "UID: ${user.uid}"

        // İstatistikler (Eğer veriler User modelinde yoksa 0 yazar)
        tvWords.text = "0" // User modeline 'totalWordsRead' eklersen burayı güncelle
        tvRevenue.text = "₺${user.totalRevenue}"
        tvStreak.text = "0" // User modeline 'streakDays' eklersen burayı güncelle

        tvStoriesCount.text = "${user.storiesCompleted} Tamamlanan / ${user.storiesStarted} Başlanan"

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
        tvRegDate.text = try { dateFormat.format(Date(user.registrationDate)) } catch (e:Exception) {"-"}
        tvLastLogin.text = "Bugün" // Son giriş tarihi modelde yoksa şimdilik statik

        // --- BUTON VE SWITCH DURUMLARI ---

        // 1. Premium
        switchPremium.setOnCheckedChangeListener(null) // Listener çakışmasını önle
        switchPremium.isChecked = user.isPremium
        if (user.isPremium) {
            ivIcon.setColorFilter(Color.parseColor("#FFD700"))
        } else {
            ivIcon.setColorFilter(Color.GRAY)
        }

        switchPremium.setOnCheckedChangeListener { _, isChecked ->
            updatePremiumStatus(isChecked, ivIcon)
        }

        // 2. Rol Değiştirme (Örnek Mantık)
        btnRole.setOnClickListener {
            Toast.makeText(context, "Rol değiştirme özelliği yakında!", Toast.LENGTH_SHORT).show()
        }

        // 3. Banlama (Şimdilik Premium gibi bir alan günceller, modelde 'isBanned' varsa onu kullan)
        switchBan.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "Ban durumu: $isChecked (Veritabanı alanı eklenmeli)", Toast.LENGTH_SHORT).show()
        }

        // 4. Şifre Sıfırlama
        btnResetPass.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.email)
                .addOnSuccessListener {
                    Toast.makeText(context, "Sıfırlama maili gönderildi!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 5. Hesabı Sil
        btnDelete.setOnClickListener {
            deleteUserPermanently()
        }

        // 6. Kapat
        btnClose.setOnClickListener { dismiss() }
    }

    private fun updatePremiumStatus(isActive: Boolean, icon: ImageView) {
        db.collection("users").document(user.uid)
            .update("isPremium", isActive)
            .addOnSuccessListener {
                if (isActive) {
                    icon.setColorFilter(Color.parseColor("#FFD700"))
                    Toast.makeText(context, "Premium Verildi ✅", Toast.LENGTH_SHORT).show()
                } else {
                    icon.setColorFilter(Color.GRAY)
                    Toast.makeText(context, "Premium Alındı ❌", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata oluştu!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUserPermanently() {
        AlertDialog.Builder(context)
            .setTitle("Kullanıcıyı Sil")
            .setMessage("Bu işlem geri alınamaz. Emin misiniz?")
            .setPositiveButton("Evet, Sil") { _, _ ->
                db.collection("users").document(user.uid).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Kullanıcı Silindi.", Toast.LENGTH_SHORT).show()
                        dismiss() // Pencereyi kapat
                    }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}