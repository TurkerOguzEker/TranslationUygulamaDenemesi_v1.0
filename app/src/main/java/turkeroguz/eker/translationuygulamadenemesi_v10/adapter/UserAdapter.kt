package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemUserManageBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserAdapter(
    private val userList: List<User>,
    private val onRoleChange: (String, String) -> Unit // (userId, newRole)
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvUserEmail.text = user.email
        holder.binding.tvUserRole.text = "Role: ${user.role}"

        holder.binding.btnMakeAdmin.setOnClickListener { onRoleChange(user.uid, "admin") }
        holder.binding.btnMakeAuthor.setOnClickListener { onRoleChange(user.uid, "author") }
    }

    override fun getItemCount() = userList.size
}