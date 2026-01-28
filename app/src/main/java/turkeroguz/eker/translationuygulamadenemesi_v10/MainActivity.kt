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
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.LoginFragment
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: LinearLayout
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }
            setBottomNavVisibility(true)

            // --- EKSİK OLAN PARÇA BURASIYDI: Giriş Tarihini Güncelle ---
            updateLastLoginAndStreak()
        } else {
            if (savedInstanceState == null) {
                replaceFragment(LoginFragment())
            }
            setBottomNavVisibility(false)
        }

        setupBottomNav()
    }

    // --- BU FONKSİYON SİZİN KODUNUZDA EKSİKTİ ---
    private fun updateLastLoginAndStreak() {
        val currentUser = auth.currentUser ?: return
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java) ?: return@addOnSuccessListener
                val now = System.currentTimeMillis()

                // 1. Son Giriş Tarihini Kesinlikle Güncelle
                val updates = hashMapOf<String, Any>("lastLoginDate" to now)

                // 2. Seri (Streak) Kontrolü
                val lastDate = java.util.Calendar.getInstance().apply { timeInMillis = if(user.lastLoginDate > 0) user.lastLoginDate else 0 }
                val today = java.util.Calendar.getInstance()

                val isSameDay = lastDate.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) &&
                        lastDate.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)

                if (!isSameDay) {
                    val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
                    val isConsecutive = lastDate.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR)

                    var newStreak = user.streakDays
                    if (isConsecutive || user.lastLoginDate == 0L) {
                        newStreak += 1
                        logActivity(currentUser.uid, "GÜNLÜK SERİ", "Tebrikler! Seri $newStreak gün oldu. 🔥", "success")
                    } else {
                        newStreak = 1
                        logActivity(currentUser.uid, "SERİ BOZULDU", "Dün girmediğiniz için seri sıfırlandı.", "warning")
                    }
                    updates["streakDays"] = newStreak
                }

                // 3. Veritabanına Yaz
                userRef.update(updates)
            }
        }
    }

    private fun logActivity(uid: String, action: String, details: String, type: String) {
        val log = hashMapOf(
            "action" to action,
            "details" to details,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to type
        )
        db.collection("users").document(uid).collection("logs").add(log)
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.btnHome).setOnClickListener { replaceFragment(HomeFragment()) }
        findViewById<View>(R.id.btnSearch).setOnClickListener { replaceFragment(BooksFragment()) }
        findViewById<View>(R.id.btnMyBooks).setOnClickListener { replaceFragment(MyBooksFragment()) }
        findViewById<View>(R.id.btnWords).setOnClickListener { replaceFragment(WordsFragment()) }
        findViewById<View>(R.id.btnSettings).setOnClickListener { replaceFragment(SettingsFragment()) }
    }

    // --- DİĞER STANDART FONKSİYONLAR ---
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: android.content.Intent) {
        val data = intent.data
        if (data != null && data.toString().contains("mode=resetPassword")) {
            val oobCode = data.getQueryParameter("oobCode")
            if (oobCode != null) {
                val fragment = turkeroguz.eker.translationuygulamadenemesi_v10.ui.ForgotPasswordFragment()
                val bundle = Bundle()
                bundle.putString("oobCode", oobCode)
                fragment.arguments = bundle
                replaceFragment(fragment)
            }
        }
    }

    fun setBottomNavVisibility(isVisible: Boolean) {
        if (::bottomNav.isInitialized) bottomNav.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun navigateToBooksSearch(query: String) {
        val fragment = BooksFragment().apply {
            arguments = Bundle().apply {
                putString("search_query", query)
            }
        }
        replaceFragment(fragment)
    }

    fun checkUserAndNavigate() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            replaceFragment(LoginFragment())
            setBottomNavVisibility(false)
        } else {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    replaceFragment(HomeFragment())
                    setBottomNavVisibility(true)
                    updateLastLoginAndStreak()
                }
                .addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        replaceFragment(HomeFragment())
                        setBottomNavVisibility(true)
                    }
                }
        }
    }

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
                    Glide.with(this).load(user.photoUrl).circleCrop().into(ivSheetProfile)
                } else {
                    val initial = nameFromEmail.firstOrNull()?.toString()?.uppercase() ?: "?"
                    val letterBitmap = createProfileBitmap(initial)
                    Glide.with(this).load(letterBitmap).circleCrop().into(ivSheetProfile)
                }
            }
        } else {
            tvName?.text = "Misafir"
            tvEmail?.text = ""
            tvId?.text = "Giriş Yapılmadı"
        }

        val themeSwitch = view.findViewById<MaterialSwitch>(R.id.themeSwitch)
        val themeSwitchContainer = view.findViewById<LinearLayout>(R.id.themeSwitchContainer)
        themeSwitchContainer?.setOnClickListener { themeSwitch.isChecked = !themeSwitch.isChecked }
        themeSwitch?.let {
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            it.isChecked = isNightMode
            it.setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        view.findViewById<View>(R.id.btnClose)?.setOnClickListener { dialog.dismiss() }
        view.findViewById<View>(R.id.btnSheetLogout)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            dialog.dismiss()
            checkUserAndNavigate()
            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.tvSelectLanguage)?.setOnClickListener { showLanguageSelectionDialog(it) }
        dialog.show()
    }

    private fun getNameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.substringBefore("@").replace(".", " ").split(" ")
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