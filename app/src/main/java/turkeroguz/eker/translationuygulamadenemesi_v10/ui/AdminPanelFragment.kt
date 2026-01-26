package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.UserAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class AdminPanelFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val allUsers = ArrayList<User>()
    private lateinit var adapter: UserAdapter

    // UI
    private lateinit var layoutDashboard: LinearLayout
    private lateinit var layoutUserList: LinearLayout
    private lateinit var chartUserTypes: PieChart
    private lateinit var chartStorySuccess: BarChart

    // Stats TextViews (XML'de eklenmeli, burada örnek gösterim)
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvDropOffRate: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- UI BAĞLAMA ---
        layoutDashboard = view.findViewById(R.id.layoutDashboard)
        layoutUserList = view.findViewById(R.id.layoutUserList)
        chartUserTypes = view.findViewById(R.id.chartUserTypes)
        chartStorySuccess = view.findViewById(R.id.chartReadingStats)
        tvTotalRevenue = view.findViewById(R.id.tvTotalUsers) // Yerine kullanıyorum
        tvDropOffRate = view.findViewById(R.id.tvPremiumUsers) // Yerine kullanıyorum

        setupTabs(view)
        setupRecyclerView(view)
        fetchDataAndCalculateStats()
    }

    private fun setupTabs(view: View) {
        view.findViewById<Button>(R.id.btnTabDashboard).setOnClickListener {
            layoutDashboard.visibility = View.VISIBLE
            layoutUserList.visibility = View.GONE
        }
        view.findViewById<Button>(R.id.btnTabUsers).setOnClickListener {
            layoutDashboard.visibility = View.GONE
            layoutUserList.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvUserList)
        rv.layoutManager = LinearLayoutManager(context)

        adapter = UserAdapter(allUsers,
            onUserClick = { user -> showEditUserDialog(user) },
            onPremiumToggle = { user, isActive -> togglePremium(user, isActive) }
        )
        rv.adapter = adapter
    }

    private fun fetchDataAndCalculateStats() {
        db.collection("users").get().addOnSuccessListener { result ->
            allUsers.clear()

            // İstatistik Değişkenleri
            var totalRevenue = 0.0
            var totalStoriesStarted = 0
            var totalStoriesCompleted = 0
            var premiumUsers = 0
            var standardUsers = 0

            for (document in result) {
                val user = document.toObject(User::class.java)
                val finalUser = if (user.uid.isEmpty()) user.copy(uid = document.id) else user
                allUsers.add(finalUser)

                // Hesaplamalar
                totalRevenue += finalUser.totalRevenue
                totalStoriesStarted += finalUser.storiesStarted
                totalStoriesCompleted += finalUser.storiesCompleted
                if (finalUser.isPremium) premiumUsers++ else standardUsers++
            }

            adapter.notifyDataSetChanged()

            // --- GRAFİKLERİ GÜNCELLE ---
            updatePieChart(premiumUsers, standardUsers)
            updateBarChart(allUsers) // Hikaye başarısı

            // --- KARTLARI GÜNCELLE ---
            tvTotalRevenue.text = "₺${totalRevenue}\nToplam Gelir"

            val dropOff = if(totalStoriesStarted > 0)
                100 - ((totalStoriesCompleted.toFloat() / totalStoriesStarted) * 100).toInt()
            else 0
            tvDropOffRate.text = "%$dropOff\nHikaye Bırakma"
        }
    }

    // --- KULLANICI DÜZENLEME (POPUP) ---
    private fun showEditUserDialog(user: User) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_edit_user, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditName)
        val etPass = dialogView.findViewById<TextInputEditText>(R.id.etEditPassword)

        etName.setText(user.name.ifEmpty { "İsimsiz" })

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSaveUser).setOnClickListener {
            val newName = etName.text.toString()
            val newPass = etPass.text.toString()

            val updates = hashMapOf<String, Any>("name" to newName)
            if (newPass.isNotEmpty()) {
                // Not: Gerçek auth şifresini değiştirmek için Firebase Auth API gerekir.
                // Burada sadece placeholder güncelliyoruz veya admin notu alıyoruz.
                updates["passwordPlaceholder"] = newPass
            }

            db.collection("users").document(user.uid).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Kullanıcı güncellendi", Toast.LENGTH_SHORT).show()
                    fetchDataAndCalculateStats() // Listeyi yenile
                    dialog.dismiss()
                }
        }
        dialog.show()
    }

    private fun togglePremium(user: User, isActive: Boolean) {
        db.collection("users").document(user.uid)
            .update("isPremium", isActive, "hasPurchasedBefore", true) // Bir kere aldıysa history true olur
            .addOnSuccessListener {
                Toast.makeText(context, "Premium güncellendi", Toast.LENGTH_SHORT).show()
                // Listeyi tamamen yenilemeye gerek yok, UI switch zaten değişti ama veri bütünlüğü için:
                fetchDataAndCalculateStats()
            }
    }

    // --- CHART FONKSİYONLARI ---
    private fun updatePieChart(premium: Int, standard: Int) {
        val entries = listOf(
            PieEntry(premium.toFloat(), "Premium"),
            PieEntry(standard.toFloat(), "Standart")
        )
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.parseColor("#FFD700"), Color.parseColor("#BDBDBD"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        chartUserTypes.data = PieData(dataSet)
        chartUserTypes.centerText = "Kullanıcı\nDağılımı"
        chartUserTypes.description.isEnabled = false
        chartUserTypes.invalidate()
    }

    private fun updateBarChart(users: List<User>) {
        // En başarılı 5 kullanıcıyı hikaye tamamlama oranına göre göster
        val topUsers = users.sortedByDescending { it.storiesCompleted }.take(5)
        val entries = ArrayList<BarEntry>()

        topUsers.forEachIndexed { index, user ->
            entries.add(BarEntry(index.toFloat(), user.storiesCompleted.toFloat()))
        }

        val dataSet = BarDataSet(entries, "En Çok Okuyanlar")
        dataSet.color = Color.parseColor("#4CAF50")

        chartStorySuccess.data = BarData(dataSet)
        chartStorySuccess.description.isEnabled = false
        chartStorySuccess.invalidate()
    }
}