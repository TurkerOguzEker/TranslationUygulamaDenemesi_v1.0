package turkeroguz.eker.translationuygulamadenemesi_v10.ui

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

    // View Tanımlamaları
    private lateinit var tvDetailName: TextView
    private lateinit var tvDetailEmail: TextView
    private lateinit var tvDetailUid: TextView
    private lateinit var switchPremiumDetail: MaterialSwitch
    private lateinit var switchBan: MaterialSwitch
    private lateinit var btnCloseSheet: Button

    // Veritabanı bağlantısı
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Doğru layout dosyasını şişiriyoruz
        return inflater.inflate(R.layout.layout_user_detail_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. XML ID'leri ile Eşleştirme (Burası düzeltildi)
        tvDetailName = view.findViewById(R.id.tvDetailName)
        tvDetailEmail = view.findViewById(R.id.tvDetailEmail)
        tvDetailUid = view.findViewById(R.id.tvDetailUid) // XML'deki adı: tvDetailUid

        switchPremiumDetail = view.findViewById(R.id.switchPremiumDetail) // XML'deki adı: switchPremiumDetail
        switchBan = view.findViewById(R.id.switchBan)
        btnCloseSheet = view.findViewById(R.id.btnCloseSheet)

        // 2. Mevcut Verileri Yükle
        loadUserData()

        // 3. Premium Switch Dinleyicisi (Değiştiği an veritabanını günceller)
        switchPremiumDetail.setOnCheckedChangeListener { _, isChecked ->
            updatePremiumStatus(isChecked)
        }

        // 4. Ban Switch Dinleyicisi
        switchBan.setOnCheckedChangeListener { _, isChecked ->
            updateBanStatus(isChecked)
        }

        // 5. Kapatma Butonu
        btnCloseSheet.setOnClickListener {
            dismiss()
        }
    }

    private fun loadUserData() {
        tvDetailName.text = user.name
        tvDetailEmail.text = user.email
        tvDetailUid.text = "UID: ${user.uid}"

        // Detay sayfasındaki switch'i veritabanı verisine göre aç
        switchPremiumDetail.isChecked = user.isPremium // Burayı kontrol edin
        switchBan.isChecked = user.isBanned
    }

    private fun updatePremiumStatus(isPremium: Boolean) {
        if (user.uid.isNotEmpty()) {
            db.collection("users").document(user.uid)
                .update("isPremium", isPremium)
                .addOnSuccessListener {
                    val durum = if (isPremium) "Aktif" else "Pasif"
                    Toast.makeText(context, "Premium: $durum", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Güncelleme hatası!", Toast.LENGTH_SHORT).show()
                    // Hata olursa switch'i eski haline getir
                    switchPremiumDetail.isChecked = !isPremium
                }
        }
    }

    private fun updateBanStatus(isBanned: Boolean) {
        if (user.uid.isNotEmpty()) {
            db.collection("users").document(user.uid)
                .update("isBanned", isBanned)
                .addOnSuccessListener {
                    val durum = if (isBanned) "Engellendi" else "Engel Kaldırıldı"
                    Toast.makeText(context, "Kullanıcı $durum", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Güncelleme hatası!", Toast.LENGTH_SHORT).show()
                    switchBan.isChecked = !isBanned
                }
        }
    }
}