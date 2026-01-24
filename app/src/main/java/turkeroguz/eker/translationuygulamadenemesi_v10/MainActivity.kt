package turkeroguz.eker.translationuygulamadenemesi_v10

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.materialswitch.MaterialSwitch
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.LanguageAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Language
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
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
        view.findViewById<View>(R.id.btnSheetLogout)?.setOnClickListener {
            dialog.dismiss()
            finish()
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
}