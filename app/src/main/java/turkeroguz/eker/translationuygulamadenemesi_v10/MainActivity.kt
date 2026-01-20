package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val featuredList = listOf(
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background)
        )

        // Modern Profil Paneli Kurulumu
        val btnProfile = findViewById<View>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.layout_profile_sheet, null)
            dialog.setContentView(view)

            view.findViewById<View>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
            view.findViewById<View>(R.id.btnSheetLogout).setOnClickListener {
                dialog.dismiss()
                finish()
            }
            dialog.show()
        }

        // Seviye Kurulumları
        setupLevelSection(R.id.sectionA1, "Beginner / Başlangıç (A1)", featuredList)
        setupLevelSection(R.id.sectionA2, "Elementary / Temel (A2)", featuredList)
        setupLevelSection(R.id.sectionB1, "Pre-Intermediate / Orta Altı (B1)", featuredList)
        setupLevelSection(R.id.sectionB1Plus, "Intermediate / Orta (B1+)", featuredList)
        setupLevelSection(R.id.sectionB2, "Upper Intermediate / Orta Üstü (B2)", featuredList)
        setupLevelSection(R.id.sectionC1, "Advanced / İleri (C1)", featuredList)
        setupLevelSection(R.id.sectionC2, "Proficiency / Profesyonel (C2)", featuredList)
    }

    private fun setupLevelSection(sectionId: Int, title: String, books: List<FeaturedBook>) {
        val sectionView = findViewById<View>(sectionId) ?: return
        sectionView.findViewById<TextView>(R.id.txtLevelTitle).text = title
        val viewPager = sectionView.findViewById<ViewPager2>(R.id.viewPagerBooks)
        viewPager.adapter = FeaturedAdapter(books)
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer { page, position ->
            val absPos = abs(position)
            page.scaleY = 0.90f + (1 - absPos) * 0.10f
            page.translationX = -position * (page.width / 1.3f)
            page.alpha = 0.4f + (1 - absPos) * 0.6f
        }
    }
}