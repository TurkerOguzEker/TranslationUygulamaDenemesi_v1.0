package turkeroguz.eker.translationuygulamadenemesi_v10

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.LanguageAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Language
import java.util.Locale
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.LoginFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.RegisterFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // Uygulama açılınca direkt kullanıcı kontrolü yap:
            checkUserAndNavigate()
        }

        findViewById<View>(R.id.btnHome).setOnClickListener {
            replaceFragment(HomeFragment())
        }

        findViewById<View>(R.id.btnSearch).setOnClickListener {
            replaceFragment(BooksFragment())
        }

        findViewById<View>(R.id.btnMyBooks).setOnClickListener {
            replaceFragment(MyBooksFragment())
        }

        findViewById<View>(R.id.btnWords).setOnClickListener {
            replaceFragment(WordsFragment())
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            replaceFragment(SettingsFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun navigateToBooksSearch(query: String) {
        val fragment = BooksFragment().apply {
            arguments = Bundle().apply {
                putString("search_query", query)
            }
        }
        replaceFragment(fragment)
        findViewById<View>(R.id.btnSearch).performClick()
    }

    fun showProfileDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_profile_sheet, null)
        dialog.setContentView(view)

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
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        view.findViewById<View>(R.id.btnClose)?.setOnClickListener { dialog.dismiss() }

        // DÜZELTME: Burada sadece tek bir listener bıraktık (Firebase Çıkış İşlemi)
        view.findViewById<View>(R.id.btnSheetLogout)?.setOnClickListener {
            // Firebase oturumunu kapat
            FirebaseAuth.getInstance().signOut()

            // Dialogu kapat
            dialog.dismiss()

            // Kullanıcıyı hemen Giriş ekranına at (HomeFragment yerine LoginFragment açılacak)
            checkUserAndNavigate()

            // İsteğe bağlı: Bilgi mesajı
            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    fun showLanguageSelectionDialog(view: View) {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_language_selection, null)
        dialog.setContentView(dialogView)

        val languages = listOf(
            Language("English", "en"),
            Language("Türkçe", "tr")
        )

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

    fun checkUserAndNavigate() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Giriş yapmamışsa Login ekranına at
            replaceFragment(LoginFragment())
        } else {
            // Giriş yapmışsa rolünü kontrol et
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    // Aktivite kapanmışsa işlemi durdur (Çökme önleyici)
                    if (isFinishing || isDestroyed) return@addOnSuccessListener

                    if (document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            Toast.makeText(this, "Yönetici Girişi", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Ana sayfayı aç
                    replaceFragment(HomeFragment())
                }
                .addOnFailureListener {
                    // Aktivite kapanmışsa işlemi durdur
                    if (isFinishing || isDestroyed) return@addOnFailureListener

                    // Hata olsa bile kullanıcının uygulamaya girmesine izin ver (veya hata mesajı göster)
                    replaceFragment(HomeFragment())
                }
        }
    }
}