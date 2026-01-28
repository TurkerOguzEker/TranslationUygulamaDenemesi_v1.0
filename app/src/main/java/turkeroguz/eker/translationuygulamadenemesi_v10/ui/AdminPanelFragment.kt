package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.UserAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class AdminPanelFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val allUsers = ArrayList<User>()
    private lateinit var adapter: UserAdapter

    // Canlı dinleyiciyi durdurmak için kayıt değişkeni
    private var statsListener: ListenerRegistration? = null

    // UI Elementleri
    private lateinit var layoutDashboard: LinearLayout
    private lateinit var layoutUserList: LinearLayout
    private lateinit var btnTabDashboard: Button
    private lateinit var btnTabUsers: Button
    private lateinit var chartUserTypes: PieChart
    private lateinit var chartStorySuccess: BarChart
    private lateinit var tvTotalUsersLabel: TextView
    private lateinit var tvPremiumUsersLabel: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupTabs()
        setupRecyclerView(view)

        // KRİTİK NOKTA: Verileri canlı izlemeye başla
        startRealtimeUpdates()
    }

    private fun initViews(view: View) {
        layoutDashboard = view.findViewById(R.id.layoutDashboard)
        layoutUserList = view.findViewById(R.id.layoutUserList)
        btnTabDashboard = view.findViewById(R.id.btnTabDashboard)
        btnTabUsers = view.findViewById(R.id.btnTabUsers)
        progressBar = view.findViewById(R.id.progressBarLoading)
        etSearch = view.findViewById(R.id.etSearchUser)
        chartUserTypes = view.findViewById(R.id.chartUserTypes)
        chartStorySuccess = view.findViewById(R.id.chartReadingStats)

        // XML ID eşleşmeleri
        tvTotalUsersLabel = view.findViewById(R.id.tvTotalUsers)
        tvPremiumUsersLabel = view.findViewById(R.id.tvPremiumUsers)
    }

    private fun startRealtimeUpdates() {
        progressBar.visibility = View.VISIBLE

        // .get() YERİNE .addSnapshotListener KULLANIYORUZ
        statsListener = db.collection("users").addSnapshotListener { result, error ->
            progressBar.visibility = View.GONE
            if (error != null || result == null) return@addSnapshotListener

            val userList = ArrayList<User>()
            var premiumCount = 0
            var revenue = 0.0

            for (document in result) {
                // UID'yi döküman ID'sinden alarak garantiye alıyoruz
                val user = document.toObject(User::class.java).copy(uid = document.id)
                userList.add(user)

                if (user.isPremium) premiumCount++
                revenue += user.totalRevenue
            }

            // Listeyi güncelle (Switchlerin doğru gözükmesi için bu şart)
            allUsers.clear()
            allUsers.addAll(userList)
            adapter.clearAndSetData(userList)

            // İstatistikleri Anlık Güncelle
            tvTotalUsersLabel.text = "${userList.size}\nToplam Kullanıcı"
            tvPremiumUsersLabel.text = "$premiumCount\nPremium Üye"

            updatePieChart(premiumCount, userList.size - premiumCount)
            updateBarChart(userList)
        }
    }

    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvUserList)
        rv.layoutManager = LinearLayoutManager(context)

        adapter = UserAdapter(
            allUsers,
            onUserClick = { user -> UserDetailBottomSheet(user).show(parentFragmentManager, "UserDetail") },
            onDeleteClick = { user -> deleteUser(user) }
        )
        rv.adapter = adapter

        // Arama kutusu dinleyicisi
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                if (query.isNotEmpty()) {
                    // Yerel listede filtreleme yap (daha hızlı)
                    val filteredList = allUsers.filter {
                        it.email.lowercase().contains(query) || it.name.lowercase().contains(query)
                    }
                    adapter.clearAndSetData(filteredList)
                } else {
                    adapter.clearAndSetData(allUsers)
                }
            }
        })
    }

    private fun setupTabs() {
        btnTabDashboard.setOnClickListener {
            layoutDashboard.visibility = View.VISIBLE
            layoutUserList.visibility = View.GONE
        }
        btnTabUsers.setOnClickListener {
            layoutDashboard.visibility = View.GONE
            layoutUserList.visibility = View.VISIBLE
        }
    }

    private fun deleteUser(user: User) {
        if (user.role == "admin") {
            Toast.makeText(context, "Admin silinemez!", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(context)
            .setTitle("Kullanıcıyı Sil")
            .setMessage("${user.email} silinsin mi?")
            .setPositiveButton("Evet") { _, _ -> db.collection("users").document(user.uid).delete() }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updatePieChart(premium: Int, standard: Int) {
        val entries = ArrayList<PieEntry>()
        if (premium > 0) entries.add(PieEntry(premium.toFloat(), "Premium"))
        if (standard > 0) entries.add(PieEntry(standard.toFloat(), "Standart"))

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#FFD700"), Color.GRAY)
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }
        chartUserTypes.data = PieData(dataSet)
        chartUserTypes.description.isEnabled = false
        chartUserTypes.animateY(800)
        chartUserTypes.invalidate()
    }

    private fun updateBarChart(users: List<User>) {
        val topUsers = users.sortedByDescending { it.storiesCompleted }.take(5)
        val entries = topUsers.mapIndexed { i, u -> BarEntry(i.toFloat(), u.storiesCompleted.toFloat()) }
        val dataSet = BarDataSet(entries, "Bitirilen Hikayeler").apply { color = Color.parseColor("#4CAF50") }
        chartStorySuccess.data = BarData(dataSet)
        chartStorySuccess.description.isEnabled = false
        chartStorySuccess.animateY(800)
        chartStorySuccess.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statsListener?.remove()
    }
}