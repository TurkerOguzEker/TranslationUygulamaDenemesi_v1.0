package turkeroguz.eker.translationuygulamadenemesi_v10

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.LanguageAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Language
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User // EKLENDİ
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.LoginFragment
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Navbar referansı (Login/Register ekranlarında gizlemek için)
    private lateinit var bottomNav: LinearLayout

    // Firebase referansları (Sınıf seviyesinde tanımlandı)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)

        // --- GİRİŞ KONTROLÜ ---
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Kullanıcı giriş yapmış -> Ana Sayfaya git
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }
            setBottomNavVisibility(true)

            // Uygulama açıldığında seri kontrolü yap
            checkUserStreak()
        } else {
            // Giriş yapmamış -> Login Ekranına git
            if (savedInstanceState == null) {
                replaceFragment(LoginFragment())
            }
            setBottomNavVisibility(false)
        }

        // --- ALT MENÜ BUTONLARI ---
        findViewById<View>(R.id.btnHome).setOnClickListener { replaceFragment(HomeFragment()) }
        findViewById<View>(R.id.btnSearch).setOnClickListener { replaceFragment(BooksFragment()) }
        findViewById<View>(R.id.btnMyBooks).setOnClickListener { replaceFragment(MyBooksFragment()) }
        findViewById<View>(R.id.btnWords).setOnClickListener { replaceFragment(WordsFragment()) }
        findViewById<View>(R.id.btnSettings).setOnClickListener { replaceFragment(SettingsFragment()) }
    } // onCreate BURADA BİTİYOR

    // --- FONKSİYONLAR BURADA (ONCREATE DIŞINDA) OLMALI ---

    private fun checkUserStreak() {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java) ?: return@addOnSuccessListener

                val today = java.util.Calendar.getInstance()
                val lastLogin = java.util.Calendar.getInstance()
                lastLogin.timeInMillis = user.lastLoginDate

                // Gün farkını hesapla
                val isSameDay = today.get(java.util.Calendar.DAY_OF_YEAR) == lastLogin.get(java.util.Calendar.DAY_OF_YEAR) &&
                        today.get(java.util.Calendar.YEAR) == lastLogin.get(java.util.Calendar.YEAR)

                // val isNextDay = today.timeInMillis - user.lastLoginDate < (24 * 60 * 60 * 1000) + (1000 * 60 * 60 * 12) // Yaklaşık kontrol (İsteğe bağlı kullanılabilir)

                if (!isSameDay) {
                    // Bugün ilk giriş
                    var newStreak = if (today.get(java.util.Calendar.DAY_OF_YEAR) - lastLogin.get(java.util.Calendar.DAY_OF_YEAR) == 1) {
                        user.streakDays + 1
                    } else {
                        1 // Seri bozulmuş, başa dön
                    }

                    // Veritabanını güncelle
                    userRef.update(
                        mapOf(
                            "lastLoginDate" to System.currentTimeMillis(),
                            "streakDays" to newStreak
                        )
                    )

                    // Günlük Giriş Logu At
                    logActivity(currentUser.uid, "Günlük Giriş", "Kullanıcı uygulamayı açtı. Seri: $newStreak")
                }
            }
        }
    }

    // --- LOGLAMA FONKSİYONU ---
    private fun logActivity(uid: String, action: String, details: String, type: String = "info") {
        val log = hashMapOf(
            "action" to action,
            "details" to details,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to type
        )
        db.collection("users").document(uid).collection("logs").add(log)
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: android.content.Intent) {
        val data = intent.data
        if (data != null && data.toString().contains("mode=resetPassword")) {
            // Linkten gelen özel kodu (oobCode) al
            val oobCode = data.getQueryParameter("oobCode")

            if (oobCode != null) {
                // Şifre Sıfırlama Sayfasını Özel Modda Aç
                val fragment = turkeroguz.eker.translationuygulamadenemesi_v10.ui.ForgotPasswordFragment()
                val bundle = Bundle()
                bundle.putString("oobCode", oobCode) // Kodu sayfaya gönderiyoruz
                fragment.arguments = bundle

                replaceFragment(fragment)
            }
        }
    }

    // --- NAVBAR GİZLEME/GÖSTERME ---
    fun setBottomNavVisibility(isVisible: Boolean) {
        if (::bottomNav.isInitialized) {
            bottomNav.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    // --- FRAGMENT YÖNETİMİ ---

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Arama sayfasına parametre ile gitmek için
    fun navigateToBooksSearch(query: String) {
        val fragment = BooksFragment().apply {
            arguments = Bundle().apply {
                putString("search_query", query)
            }
        }
        replaceFragment(fragment)
    }

    // --- KULLANICI KONTROLÜ VE YÖNLENDİRME ---
    fun checkUserAndNavigate() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            replaceFragment(LoginFragment())
            setBottomNavVisibility(false) // Çıkış yapınca navbar gizlenmeli
        } else {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    if (document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            Toast.makeText(this, "Yönetici Girişi", Toast.LENGTH_SHORT).show()
                        }
                    }
                    replaceFragment(HomeFragment())
                    setBottomNavVisibility(true) // Giriş yapınca navbar görünmeli
                }
                .addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        replaceFragment(HomeFragment())
                        setBottomNavVisibility(true)
                    }
                }
        }
    }

    // --- PROFİL PENCERESİ (Bottom Sheet) ---
    fun showProfileDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_profile_sheet, null)
        dialog.setContentView(view)

        val user = FirebaseAuth.getInstance().currentUser

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvId = view.findViewById<TextView>(R.id.tvProfileId)
        val ivSheetProfile = view.findViewById<ImageView>(R.id.ivProfileImage)

        if (user != null) {
            val email = user.email ?: ""
            val nameFromEmail = getNameFromEmail(email)

            tvName?.text = nameFromEmail
            tvEmail?.text = email
            tvId?.text = "ID: ${user.uid}"

            if (ivSheetProfile != null) {
                if (user.photoUrl != null) {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .circleCrop()
                        .into(ivSheetProfile)
                } else {
                    val initial = nameFromEmail.firstOrNull()?.toString()?.uppercase() ?: "?"
                    val letterBitmap = createProfileBitmap(initial)

                    Glide.with(this)
                        .load(letterBitmap)
                        .circleCrop()
                        .into(ivSheetProfile)
                }
            }
        } else {
            tvName?.text = "Misafir"
            tvEmail?.text = ""
            tvId?.text = "Giriş Yapılmadı"
        }

        // Tema Ayarları
        val themeSwitch = view.findViewById<MaterialSwitch>(R.id.themeSwitch)
        val themeIcon = view.findViewById<ImageView>(R.id.ivThemeIcon)
        val themeSwitchContainer = view.findViewById<LinearLayout>(R.id.themeSwitchContainer)

        themeSwitchContainer?.setOnClickListener {
            themeSwitch.isChecked = !themeSwitch.isChecked
        }

        themeSwitch?.let {
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            it.isChecked = isNightMode
            if (isNightMode) themeIcon?.setImageResource(android.R.drawable.ic_menu_recent_history)

            it.setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        view.findViewById<View>(R.id.btnClose)?.setOnClickListener { dialog.dismiss() }

        // ÇIKIŞ YAP Butonu
        view.findViewById<View>(R.id.btnSheetLogout)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            dialog.dismiss()
            checkUserAndNavigate()
            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
        }

        // Dil Seçimi
        view.findViewById<View>(R.id.tvSelectLanguage)?.setOnClickListener {
            showLanguageSelectionDialog(it)
        }

        dialog.show()
    }

    // --- YARDIMCI FONKSİYONLAR ---

    private fun getNameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.substringBefore("@")
                .replace(".", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        } else {
            "Kullanıcı"
        }
    }

    private fun createProfileBitmap(text: String): Bitmap {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        paint.color = Color.parseColor("#5C6BC0")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = Color.WHITE
        paint.textSize = 100f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        val xPos = (canvas.width / 2).toFloat()
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)

        canvas.drawText(text, xPos, yPos, paint)

        return bitmap
    }

    fun showLanguageSelectionDialog(view: View) {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_language_selection, null)
        dialog.setContentView(dialogView)

        val languages = listOf(Language("English", "en"), Language("Türkçe", "tr"))
        val currentLanguageCode = resources.configuration.locales[0].language

        val rvLanguages = dialogView.findViewById<RecyclerView>(R.id.rvLanguages)
        rvLanguages.layoutManager = LinearLayoutManager(this)
        rvLanguages.adapter = LanguageAdapter(languages, currentLanguageCode) { languageCode ->
            setLocale(languageCode)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        recreate()
    }
}