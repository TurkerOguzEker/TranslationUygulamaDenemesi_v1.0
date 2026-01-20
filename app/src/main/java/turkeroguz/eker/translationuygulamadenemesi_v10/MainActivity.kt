package turkeroguz.eker.translationuygulamadenemesi_v10

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.materialswitch.MaterialSwitch
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.FeaturedAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.FeaturedBook

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val featuredList = listOf(
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background)
        )

        findViewById<View>(R.id.btnProfile)?.setOnClickListener {
            showProfileDialog()
        }

        setupLevelSections(featuredList)
    }

    private fun showProfileDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_profile_sheet, null)
        dialog.setContentView(view)

        dialog.setOnShowListener {
            val bottomSheet = view.parent as? View
            bottomSheet?.let {
                val layoutParams = it.layoutParams
                val windowHeight = (resources.displayMetrics.heightPixels * 0.85).toInt() // Ekranın %85'i
                layoutParams.height = windowHeight
                it.layoutParams = layoutParams

                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

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

    private fun setupLevelSections(featuredList: List<FeaturedBook>) {
        setupLevelSection(R.id.sectionA1, "Beginner (A1)", featuredList)
        setupLevelSection(R.id.sectionA2, "Elementary (A2)", featuredList)
        setupLevelSection(R.id.sectionB1, "Pre-Intermediate (B1)", featuredList)
        setupLevelSection(R.id.sectionB1Plus, "Intermediate (B1+)", featuredList)
        setupLevelSection(R.id.sectionB2, "Upper Intermediate (B2)", featuredList)
        setupLevelSection(R.id.sectionC1, "Advanced (C1)", featuredList)
        setupLevelSection(R.id.sectionC2, "Proficiency (C2)", featuredList)
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

            // Kenar boşluk ayarları (Kitapların yan yana görünmesi için)
            val marginInDp = 50
            val px = (marginInDp * resources.displayMetrics.density).toInt()
            it.setPadding(px, 0, px, 0)

            it.setPageTransformer { page, position ->
                val absPos = kotlin.math.abs(position)

                // Dikdörtgen formu bozmayan ölçekleme
                val scale = 0.85f + (1 - absPos) * 0.15f
                page.scaleY = scale
                page.scaleX = scale

                // Kitapları birbirine yaklaştıran hizalama
                page.translationX = -position * (px * 3.2f)
                page.alpha = 0.5f + (1 - absPos) * 0.5f
            }
        }
    }
}
