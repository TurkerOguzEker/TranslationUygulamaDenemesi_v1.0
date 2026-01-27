package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    private var userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit, // Detay/Profil açma
    private val onDeleteClick: (User) -> Unit // Silme işlemi (Switch yerine bu geldi)
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        if (user.role == "admin") {
            holder.binding.btnDeleteUser.visibility = android.view.View.GONE
        } else {
            holder.binding.btnDeleteUser.visibility = android.view.View.VISIBLE

            // Normal kullanıcılarda tıklama özelliği çalışsın
            holder.binding.btnDeleteUser.setOnClickListener {
                onDeleteClick(user)
            }
        }
        holder.binding.tvUserName.text = user.name ?: "İsimsiz"
        holder.binding.tvUserEmail.text = user.email

        // Premium ise Altın rengi ikon, değilse gri
        if (user.isPremium) {
            holder.binding.ivUserIcon.setColorFilter(Color.parseColor("#FFD700"))
        } else {
            holder.binding.ivUserIcon.setColorFilter(Color.GRAY)
        }

        // Tıklama olayları (Detay sayfasını açar)
        holder.binding.cardRoot.setOnClickListener { onUserClick(user) }
        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }

        // SİLME BUTONU
        // Hata veren 'switchPremium' kodlarını sildik, yerine bunu ekledik:
        holder.binding.btnDeleteUser.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount() = userList.size

    fun clearAndSetData(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }
}