package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    private var userList: List<User>, // ArrayList yerine List kullanmak daha esnektir
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

        holder.binding.tvUserName.text = user.name.ifEmpty { "İsimsiz" }
        holder.binding.tvUserEmail.text = user.email

        // 1. Önce dinleyiciyi kaldır (Sonsuz döngüyü engellemek için)
        holder.binding.switchPremium.setOnCheckedChangeListener(null)

        // 2. Switch'i veritabanındaki duruma göre ayarla
        holder.binding.switchPremium.isChecked = user.isPremium

        // 3. İkon rengini ayarla
        holder.binding.ivUserIcon.setColorFilter(if (user.isPremium) Color.parseColor("#FFD700") else Color.GRAY)

        // 4. Dinleyiciyi tekrar ekle
        holder.binding.switchPremium.setOnCheckedChangeListener { _, isChecked ->
            // Sadece bu kullanıcıyı güncelle
            db.collection("users").document(user.uid).update("isPremium", isChecked)
                .addOnFailureListener {
                    // Hata olursa switch'i eski haline getir
                    holder.binding.switchPremium.isChecked = !isChecked
                    Toast.makeText(holder.itemView.context, "Güncelleme Başarısız", Toast.LENGTH_SHORT).show()
                }
        }

        // Diğer butonlar
        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }

        if (user.role == "admin") {
            holder.binding.btnDeleteUser.visibility = android.view.View.GONE
            holder.binding.switchPremium.isEnabled = false
        } else {
            holder.binding.btnDeleteUser.visibility = android.view.View.VISIBLE
            holder.binding.btnDeleteUser.setOnClickListener { onDeleteClick(user) }
            holder.binding.switchPremium.isEnabled = true
        }
    }

    override fun getItemCount() = userList.size

    fun clearAndSetData(newUsers: List<User>) {
        // Listeyi yenile ve arayüzü güncelle
        userList = newUsers
        notifyDataSetChanged()
    }
}