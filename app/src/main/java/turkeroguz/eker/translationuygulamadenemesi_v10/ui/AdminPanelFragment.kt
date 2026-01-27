package turkeroguz.eker.translationuygulamadenemesi_v10.ui

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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.UserAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class AdminPanelFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val allUsers = ArrayList<User>()
    private lateinit var adapter: UserAdapter

    // --- PAGINATION ---
    private var isLastPage = false
    private var isLoading = false

    // UI Elementleri
    private lateinit var layoutDashboard: LinearLayout
    private lateinit var layoutUserList: LinearLayout
    private lateinit var btnTabDashboard: Button
    private lateinit var btnTabUsers: Button
    private lateinit var chartUserTypes: PieChart
    private lateinit var chartStorySuccess: BarChart
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvDropOffRate: TextView
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
        fetchStatsForDashboard()
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
        tvTotalRevenue = view.findViewById(R.id.tvTotalUsers)
        tvDropOffRate = view.findViewById(R.id.tvPremiumUsers)
    }

    private fun setupTabs() {
        btnTabDashboard.setOnClickListener {
            layoutDashboard.visibility = View.VISIBLE
            layoutUserList.visibility = View.GONE
            fetchStatsForDashboard() // Dashboard açılınca verileri tazele
        }
        btnTabUsers.setOnClickListener {
            layoutDashboard.visibility = View.GONE
            layoutUserList.visibility = View.VISIBLE
            if (allUsers.isEmpty()) {
                fetchUsersResult(isNextPage = false)
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvUserList)
        rv.layoutManager = LinearLayoutManager(context)

        // ✅ DÜZELTME: Adapter'a hem tıklama hem de switch toggle fonksiyonunu gönderiyoruz
        adapter = UserAdapter(
            allUsers,
            onUserClick = { user -> showUserDetailBottomSheet(user) },
            onPremiumToggle = { user, isActive -> togglePremium(user, isActive) }
        )
        rv.adapter = adapter

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        fetchUsersResult(isNextPage = true)
                    }
                }
            }
        })

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length > 2) {
                    searchUsersInFirestore(query)
                } else if (query.isEmpty()) {
                    fetchUsersResult(isNextPage = false)
                }
            }
        })
    }

    private fun showUserDetailBottomSheet(user: User) {
        val bottomSheet = UserDetailBottomSheet(user)
        bottomSheet.show(parentFragmentManager, "UserDetailSheet")
    }

    // --- LİSTELEME ---
    private fun fetchUsersResult(isNextPage: Boolean) {
        if (isLoading) return
        isLoading = true
        progressBar.visibility = View.VISIBLE

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val newUsers = ArrayList<User>()
                for (document in result) {
                    try {
                        val user = document.toObject(User::class.java)
                        val finalUser = if (user.uid.isEmpty()) user.copy(uid = document.id) else user
                        newUsers.add(finalUser)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                allUsers.clear()
                adapter.clearAndSetData(newUsers)
                isLoading = false
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                isLoading = false
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchUsersInFirestore(searchText: String) {
        isLoading = true
        progressBar.visibility = View.VISIBLE
        db.collection("users")
            .orderBy("email")
            .startAt(searchText)
            .endAt(searchText + "\uf8ff")
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val searchResults = ArrayList<User>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    searchResults.add(user)
                }
                adapter.clearAndSetData(searchResults)
                isLoading = false
                progressBar.visibility = View.GONE
            }
    }

    // --- PREMIUM DEĞİŞTİRME (VERİTABANI) ---
    private fun togglePremium(user: User, isActive: Boolean) {
        db.collection("users").document(user.uid)
            .update("isPremium", isActive, "hasPurchasedBefore", true)
            .addOnSuccessListener {
                Toast.makeText(context, "${user.name} Premium durumu: $isActive", Toast.LENGTH_SHORT).show()
                logAction("Premium ${if(isActive) "Verildi" else "Alındı"}: ${user.email}", "PREMIUM")

                // Grafik verilerinin güncellenmesi için dashboard'u yenileyelim
                fetchStatsForDashboard()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Güncelleme Başarısız!", Toast.LENGTH_SHORT).show()
                // Hata olursa switch'i eski haline getirmek gerekebilir (Opsiyonel)
            }
    }

    private fun logAction(desc: String, type: String) {
        val log = hashMapOf(
            "description" to desc,
            "type" to type,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("admin_logs").add(log)
    }

    // --- DASHBOARD ---
    private fun fetchStatsForDashboard() {
        db.collection("users").get().addOnSuccessListener { result ->
            var totalRevenue = 0.0
            var totalStoriesStarted = 0
            var totalStoriesCompleted = 0
            var premiumUsers = 0
            var standardUsers = 0
            val statList = ArrayList<User>()

            for (document in result) {
                val user = document.toObject(User::class.java)
                statList.add(user)
                totalRevenue += user.totalRevenue
                totalStoriesStarted += user.storiesStarted
                totalStoriesCompleted += user.storiesCompleted

                if (user.isPremium) premiumUsers++ else standardUsers++
            }

            // ✅ GRAFİK GÜNCELLEME ÇAĞRISI
            updatePieChart(premiumUsers, standardUsers)
            updateBarChart(statList)

            tvTotalRevenue.text = "₺${totalRevenue.toInt()}\nToplam Gelir"
            val dropOff = if(totalStoriesStarted > 0)
                100 - ((totalStoriesCompleted.toFloat() / totalStoriesStarted) * 100).toInt()
            else 0
            tvDropOffRate.text = "%$dropOff\nHikaye Bırakma"
        }
    }

    // --- GRAFİK DÜZELTMESİ ---
    private fun updatePieChart(premium: Int, standard: Int) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // Sadece sayısı 0'dan büyük olanları grafiğe ekle
        if (premium > 0) {
            entries.add(PieEntry(premium.toFloat(), "Premium ($premium)"))
            colors.add(Color.parseColor("#FFD700")) // Altın Sarısı
        }

        if (standard > 0) {
            entries.add(PieEntry(standard.toFloat(), "Standart ($standard)"))
            colors.add(Color.parseColor("#BDBDBD")) // Gri
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        chartUserTypes.data = data
        chartUserTypes.description.isEnabled = false
        chartUserTypes.centerText = "Kullanıcılar"
        chartUserTypes.setCenterTextSize(14f)

        chartUserTypes.animateY(1000) // Animasyon ekle
        chartUserTypes.invalidate() // ✅ Grafiği çizdir
    }

    private fun updateBarChart(users: List<User>) {
        val topUsers = users.sortedByDescending { it.storiesCompleted }.take(5)
        val entries = ArrayList<BarEntry>()
        topUsers.forEachIndexed { index, user ->
            entries.add(BarEntry(index.toFloat(), user.storiesCompleted.toFloat()))
        }

        val dataSet = BarDataSet(entries, "En Çok Okuyanlar")
        dataSet.color = Color.parseColor("#4CAF50")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        chartStorySuccess.data = BarData(dataSet)
        chartStorySuccess.description.isEnabled = false
        chartStorySuccess.animateY(1000)
        chartStorySuccess.invalidate()
    }
}