package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    // Listeyi dinamik yönetebilmek için ArrayList olarak alıyoruz
    private var userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // E-posta ve İstatistikler
        holder.binding.tvUserEmail.text = user.email

        // Bölme işleminde sıfıra bölünme hatasını önlemek için kontrol
        val dropOffRate = if(user.storiesStarted > 0) (user.storiesCompleted.toFloat() / user.storiesStarted.toFloat()) * 100 else 0f

        holder.binding.tvUserStats.text = "Kelime: ${user.totalWordsLearned} | Bitirme: %${dropOffRate.toInt()}"

        // --- İKON RENGİ MANTIĞI ---
        if (user.isPremium) {
            // Aktif - Yeşil
            holder.binding.ivPremiumStatus.setColorFilter(Color.parseColor("#4CAF50"))
        } else if (user.hasPurchasedBefore) {
            // Pasif (Eski müşteri) - Gri
            holder.binding.ivPremiumStatus.setColorFilter(Color.GRAY)
        } else {
            // Hiç almamış - Kırmızı
            holder.binding.ivPremiumStatus.setColorFilter(Color.parseColor("#D32F2F"))
        }

        // Tıklama Olayları
        holder.binding.btnEditUser.setOnClickListener { onUserClick(user) }
        holder.binding.cardRoot.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = userList.size

    // --- LİSTE GÜNCELLEME FONKSİYONLARI ---

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