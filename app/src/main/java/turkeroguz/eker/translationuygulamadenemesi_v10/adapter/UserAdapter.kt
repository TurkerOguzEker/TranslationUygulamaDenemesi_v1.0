package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    private var userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit,
    // YENİ EKLENEN: Switch tetiklendiğinde çalışacak fonksiyon
    private val onPremiumToggle: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.binding.tvUserName.text = user.name ?: "İsimsiz"
        holder.binding.tvUserEmail.text = user.email

        // --- SWITCH MANTIĞI DÜZELTİLDİ ---
        // 1. Eski dinleyiciyi kaldır (Recycle hatasını önler)
        holder.binding.switchPremium.setOnCheckedChangeListener(null)

        // 2. Doğru durumu ayarla
        holder.binding.switchPremium.isChecked = user.isPremium

        // 3. Renk ayarı
        if (user.isPremium) {
            holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#FFD700")) // Altın
        } else {
            holder.binding.ivUserIcon.setColorFilter(Color.GRAY)
        }

        // 4. Yeni dinleyiciyi ekle
        holder.binding.switchPremium.setOnCheckedChangeListener { _, isChecked ->
            // Fragment'a haber ver
            onPremiumToggle(user, isChecked)

            // Görseli anlık güncelle
            if (isChecked) {
                holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#FFD700"))
            } else {
                holder.binding.ivUserIcon.setColorFilter(Color.GRAY)
            }
        }

        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }
        holder.binding.cardRoot.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = userList.size

    fun addData(newUsers: List<User>) {
        val startPos = userList.size
        userList.addAll(newUsers)
        notifyItemRangeInserted(startPos, newUsers.size)
    }

    fun clearAndSetData(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }
}