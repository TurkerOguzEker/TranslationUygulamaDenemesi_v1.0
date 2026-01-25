package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.UserAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentAdminPanelBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class AdminPanelFragment : Fragment() {
    private lateinit var binding: FragmentAdminPanelBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users").get().addOnSuccessListener { result ->
            val userList = result.toObjects(User::class.java)
            binding.rvUserList.layoutManager = LinearLayoutManager(context)
            binding.rvUserList.adapter = UserAdapter(userList) { userId, newRole ->
                updateUserRole(userId, newRole)
            }
        }
    }

    private fun updateUserRole(userId: String, newRole: String) {
        db.collection("users").document(userId).update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(context, "Rol güncellendi: $newRole", Toast.LENGTH_SHORT).show()
                fetchUsers() // Listeyi yenile
            }
    }
}