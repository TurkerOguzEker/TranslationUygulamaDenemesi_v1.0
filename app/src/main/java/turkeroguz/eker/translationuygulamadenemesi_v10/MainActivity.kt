package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.materialswitch.MaterialSwitch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private var profileDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val featuredList = listOf(
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background)
        )

        // Profil Butonu
        findViewById<View>(R.id.btnProfile)?.setOnClickListener {
            showProfileDialog()
        }

        // Seviye Kurulumları
        setupLevelSection(R.id.sectionA1, "Beginner (A1)", featuredList)
        setupLevelSection(R.id.sectionA2, "Elementary (A2)", featuredList)
        setupLevelSection(R.id.sectionB1, "Pre-Intermediate (B1)", featuredList)
        setupLevelSection(R.id.sectionB1Plus, "Intermediate (B1+)", featuredList)
        setupLevelSection(R.id.sectionB2, "Upper Intermediate (B2)", featuredList)
        setupLevelSection(R.id.sectionC1, "Advanced (C1)", featuredList)
        setupLevelSection(R.id.sectionC2, "Proficiency (C2)", featuredList)
    }

    private fun showProfileDialog() {
        profileDialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.layout_profile_sheet, null)
        profileDialog?.setContentView(view)
        profileDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val themeSwitch = view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.themeSwitch)

        // Mevcut modu kontrol et ve switch'i ayarla
        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        themeSwitch?.isChecked = isNightMode

        themeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
            profileDialog?.dismiss()
        }

        view.findViewById<View>(R.id.btnClose)?.setOnClickListener { profileDialog?.dismiss() }
        view.findViewById<View>(R.id.btnSheetLogout)?.setOnClickListener { finish() }

        profileDialog?.show()
    }

    private fun setupLevelSection(sectionId: Int, title: String, books: List<FeaturedBook>) {
        val sectionView = findViewById<View>(sectionId) ?: return
        sectionView.findViewById<TextView>(R.id.txtLevelTitle)?.text = title
        val viewPager = sectionView.findViewById<ViewPager2>(R.id.viewPagerBooks)

        viewPager?.let {
            it.adapter = FeaturedAdapter(books)
            it.clipToPadding = false
            it.clipChildren = false
            it.offscreenPageLimit = 3
            it.setPageTransformer { page, position ->
                val absPos = abs(position)
                page.scaleY = 0.90f + (1 - absPos) * 0.10f
                page.translationX = -position * (page.width / 1.3f)
                page.alpha = 0.4f + (1 - absPos) * 0.6f
            }
        }
    }
}