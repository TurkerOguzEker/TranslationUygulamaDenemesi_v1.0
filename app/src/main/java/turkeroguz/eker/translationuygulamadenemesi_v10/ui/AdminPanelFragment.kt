package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.graphics.Color // EKSİK OLAN BU SATIRDI
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
import com.google.firebase.firestore.Query
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.UserAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class AdminPanelFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val allUsers = ArrayList<User>()
    private lateinit var adapter: UserAdapter

    // --- PAGINATION DEĞİŞKENLERİ ---
    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false
    private var isLoading = false
    private val PAGE_SIZE = 10L

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

        // GÜNCELLEME: onPremiumToggle parametresi ARTIK YOK.
        adapter = UserAdapter(allUsers,
            onUserClick = { user -> showUserDetailBottomSheet(user) }
        )
        rv.adapter = adapter

        // ... (Scroll listener ve search listener kodları aynı kalacak) ...
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

    // --- BOTTOM SHEET AÇMA ---
    private fun showUserDetailBottomSheet(user: User) {
        val bottomSheet = UserDetailBottomSheet(user)
        bottomSheet.show(parentFragmentManager, "UserDetailSheet")
    }

    // --- 1. LİSTELEME & SAYFALAMA ---
    // --- HATA AYIKLAMA (DEBUG) MODU ---
    private fun fetchUsersResult(isNextPage: Boolean) {
        // Zaten yükleniyorsa tekrar çağırma
        if (isLoading) return

        isLoading = true
        progressBar.visibility = View.VISIBLE

        // 1. SORGU: Hiçbir sıralama (orderBy) veya limit koymuyoruz.
        // Amaç: Veritabanında "users" adında ne varsa hepsini çekmek.
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                // LOG: Kaç kişi bulundu?
                android.util.Log.d("AdminPanel", "TOPLAM BELGE SAYISI: ${result.size()}")

                val newUsers = ArrayList<User>()

                for (document in result) {
                    // LOG: Her bir belgenin içeriğini yazdır
                    android.util.Log.d("AdminPanel", "Belge ID: ${document.id} -> Data: ${document.data}")

                    try {
                        val user = document.toObject(User::class.java)

                        // Eğer modeldeki UID boş gelirse, döküman ID'sini kullan
                        val finalUser = if (user.uid.isEmpty()) {
                            user.copy(uid = document.id)
                        } else {
                            user
                        }

                        newUsers.add(finalUser)
                    } catch (e: Exception) {
                        android.util.Log.e("AdminPanel", "Çevirme Hatası (Model Uyuşmazlığı): ${e.message}")
                    }
                }

                // Listeyi ekrana bas
                allUsers.clear()
                adapter.clearAndSetData(newUsers)

                isLoading = false
                progressBar.visibility = View.GONE

                // Eğer liste boşsa kullanıcıya bilgi ver
                if (newUsers.isEmpty()) {
                    Toast.makeText(context, "Veritabanında 'users' koleksiyonu boş!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                // LOG: Hata mesajı
                android.util.Log.e("AdminPanel", "BAĞLANTI HATASI: ${e.message}")
                Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_LONG).show()

                isLoading = false
                progressBar.visibility = View.GONE
            }
    }

    // --- 2. ARAMA ---
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
                    val finalUser = if (user.uid.isEmpty()) user.copy(uid = document.id) else user
                    searchResults.add(finalUser)
                }
                adapter.clearAndSetData(searchResults)
                isLoading = false
                progressBar.visibility = View.GONE
            }
    }

    // --- 3. DASHBOARD İSTATİSTİKLERİ ---
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

            updatePieChart(premiumUsers, standardUsers)
            updateBarChart(statList)

            tvTotalRevenue.text = "₺${totalRevenue}\nToplam Gelir"
            val dropOff = if(totalStoriesStarted > 0)
                100 - ((totalStoriesCompleted.toFloat() / totalStoriesStarted) * 100).toInt()
            else 0
            tvDropOffRate.text = "%$dropOff\nHikaye Bırakma"
        }
    }

    private fun togglePremium(user: User, isActive: Boolean) {
        db.collection("users").document(user.uid)
            .update("isPremium", isActive, "hasPurchasedBefore", true)
            .addOnSuccessListener {
                Toast.makeText(context, "Premium güncellendi", Toast.LENGTH_SHORT).show()
                logAction("Premium ${if(isActive) "Verildi" else "Alındı"}: ${user.email}", "PREMIUM")
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

    private fun updatePieChart(premium: Int, standard: Int) {
        val entries = listOf(PieEntry(premium.toFloat(), "Premium"), PieEntry(standard.toFloat(), "Standart"))
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.parseColor("#FFD700"), Color.parseColor("#BDBDBD"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f
        chartUserTypes.data = PieData(dataSet)
        chartUserTypes.description.isEnabled = false
        chartUserTypes.invalidate()
    }

    private fun updateBarChart(users: List<User>) {
        val topUsers = users.sortedByDescending { it.storiesCompleted }.take(5)
        val entries = ArrayList<BarEntry>()
        topUsers.forEachIndexed { index, user -> entries.add(BarEntry(index.toFloat(), user.storiesCompleted.toFloat())) }
        val dataSet = BarDataSet(entries, "En Çok Okuyanlar")
        dataSet.color = Color.parseColor("#4CAF50")
        chartStorySuccess.data = BarData(dataSet)
        chartStorySuccess.description.isEnabled = false
        chartStorySuccess.invalidate()
    }
}