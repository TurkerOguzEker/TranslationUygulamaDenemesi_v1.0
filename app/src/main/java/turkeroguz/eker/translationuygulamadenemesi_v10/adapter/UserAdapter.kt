package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    private var userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // 1. Temel Bilgiler
        holder.binding.tvUserName.text = user.name ?: "İsimsiz"
        holder.binding.tvUserEmail.text = user.email

        // 2. Switch'in Mevcut Durumunu Ayarla (Kritik Düzeltme)
        holder.binding.switchPremium.setOnCheckedChangeListener(null) // Döngüyü engellemek için önce listener'ı sil
        holder.binding.switchPremium.isChecked = user.isPremium

        // 3. İkon Rengini Ayarla
        updateIconColor(holder, user.isPremium)

        // 4. Switch Değiştirildiğinde Firebase'i Güncelle
        holder.binding.switchPremium.setOnCheckedChangeListener { _, isChecked ->
            updatePremiumInFirebase(user.uid, isChecked, holder)
        }

        // 5. Admin Koruması
        if (user.role == "admin") {
            holder.binding.btnDeleteUser.visibility = View.GONE
            holder.binding.switchPremium.isEnabled = false // Adminin yetkisi değiştirilemez
        } else {
            holder.binding.btnDeleteUser.visibility = View.VISIBLE
            holder.binding.switchPremium.isEnabled = true
        }

        // 6. Tıklama Olayları
        holder.binding.btnDeleteUser.setOnClickListener { onDeleteClick(user) }
        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }
        holder.binding.cardRoot.setOnClickListener { onUserClick(user) }
    }

    private fun updatePremiumInFirebase(uid: String, isPremium: Boolean, holder: UserViewHolder) {
        if (uid.isEmpty()) return

        db.collection("users").document(uid)
            .update("isPremium", isPremium)
            .addOnSuccessListener {
                updateIconColor(holder, isPremium)
                Toast.makeText(holder.itemView.context, "Premium Durumu Güncellendi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                holder.binding.switchPremium.isChecked = !isPremium
                Toast.makeText(holder.itemView.context, "Hata: Güncellenemedi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateIconColor(holder: UserViewHolder, isPremium: Boolean) {
        if (isPremium) {
            holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#FFD700")) // Altın Rengi
        } else {
            holder.binding.ivUserIcon.setColorFilter(Color.GRAY)
        }
    }

    override fun getItemCount() = userList.size

    fun clearAndSetData(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }
}