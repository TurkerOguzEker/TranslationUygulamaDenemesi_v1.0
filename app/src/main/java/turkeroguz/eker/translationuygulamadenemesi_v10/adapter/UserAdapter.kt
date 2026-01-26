package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    // 1. DEĞİŞİKLİK: List<User> yerine ArrayList<User> yaptık ve 'val' yaptık.
    private val userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit,
    private val onPremiumToggle: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.binding.tvUserEmail.text = user.email
        // İstatistik Özeti (Sıfıra bölünme hatasını önlemek için kontrol)
        val dropOffRate = if(user.storiesStarted > 0) (user.storiesCompleted.toFloat() / user.storiesStarted.toFloat()) * 100 else 0f
        holder.binding.tvUserStats.text = "Kelime: ${user.totalWordsLearned} | Bitirme: %${dropOffRate.toInt()}"

        // --- PREMIUM IKON MANTIĞI ---
        if (user.isPremium) {
            // Aktif - Yeşil Tik
            holder.binding.ivPremiumStatus.setImageResource(android.R.drawable.checkbox_on_background)
            holder.binding.ivPremiumStatus.setColorFilter(Color.parseColor("#4CAF50"))
        } else if (user.hasPurchasedBefore) {
            // Pasif (Daha önce almış) - Nötr Gri
            holder.binding.ivPremiumStatus.setImageResource(android.R.drawable.ic_menu_help)
            holder.binding.ivPremiumStatus.setColorFilter(Color.GRAY)
        } else {
            // Hiç Almamış - Kırmızı Çarpı
            holder.binding.ivPremiumStatus.setImageResource(android.R.drawable.ic_delete)
            holder.binding.ivPremiumStatus.setColorFilter(Color.parseColor("#D32F2F"))
        }

        // Switch
        holder.binding.switchPremium.setOnCheckedChangeListener(null)
        holder.binding.switchPremium.isChecked = user.isPremium
        holder.binding.switchPremium.setOnCheckedChangeListener { _, isChecked ->
            onPremiumToggle(user, isChecked)
        }

        // Tıklama Olayları
        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }
        holder.binding.cardRoot.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = userList.size

    // --- YENİ EKLENEN FONKSİYONLAR (HATA VERENLER DÜZELTİLDİ) ---

    // Pagination için listeye ekleme yapar
    fun addData(newUsers: List<User>) {
        val startPos = userList.size
        userList.addAll(newUsers) // Artık cast etmeye gerek yok
        notifyItemRangeInserted(startPos, newUsers.size)
    }

    // Arama veya yenileme yapıldığında listeyi sıfırlar ve yenisini koyar
    fun clearAndSetData(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }

    // Eski koddan kalan updateList fonksiyonunu da uyumlu hale getirdik
    fun updateList(newList: List<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}