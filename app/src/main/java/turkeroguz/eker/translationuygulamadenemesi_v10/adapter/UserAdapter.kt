package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    private var userList: List<User>,
    private val onUserClick: (User) -> Unit, // Kullanıcıya tıklayınca (Düzenle)
    private val onPremiumToggle: (User, Boolean) -> Unit // Switch değişince
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.binding.tvUserEmail.text = user.email
        // İstatistik Özeti
        val dropOffRate = if(user.storiesStarted > 0) (user.storiesCompleted.toFloat() / user.storiesStarted.toFloat()) * 100 else 0f
        holder.binding.tvUserStats.text = "Kelime: ${user.totalWordsLearned} | Bitirme: %${dropOffRate.toInt()}"

        // --- PREMIUM IKON MANTIĞI ---
        if (user.isPremium) {
            // Aktif - Yeşil Tik
            holder.binding.ivPremiumStatus.setImageResource(android.R.drawable.checkbox_on_background)
            holder.binding.ivPremiumStatus.setColorFilter(Color.parseColor("#4CAF50")) // Yeşil
        } else if (user.hasPurchasedBefore) {
            // Pasif (Daha önce almış) - Nötr Gri
            holder.binding.ivPremiumStatus.setImageResource(android.R.drawable.ic_menu_help) // veya uygun bir nötr ikon
            holder.binding.ivPremiumStatus.setColorFilter(Color.GRAY)
        } else {
            // Hiç Almamış - Kırmızı Çarpı
            holder.binding.ivPremiumStatus.setImageResource(android.R.drawable.ic_delete)
            holder.binding.ivPremiumStatus.setColorFilter(Color.parseColor("#D32F2F")) // Kırmızı
        }

        // Switch
        holder.binding.switchPremium.setOnCheckedChangeListener(null)
        holder.binding.switchPremium.isChecked = user.isPremium
        holder.binding.switchPremium.setOnCheckedChangeListener { _, isChecked ->
            onPremiumToggle(user, isChecked)
        }

        // Tıklama Olayları
        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }
        holder.binding.root.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = userList.size

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}