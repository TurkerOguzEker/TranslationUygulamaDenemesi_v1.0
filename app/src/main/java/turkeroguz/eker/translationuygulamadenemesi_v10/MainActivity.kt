package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerFeatured)

        val featuredList = listOf(
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background)
        )

        viewPager.adapter = FeaturedAdapter(featuredList)

        // ViewPager2 Çökmesini Engelleyen Padding Ayarı
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.offscreenPageLimit = 3

        // Kartların Stack (Yığın) gibi görünmesi için Transformer
        viewPager.setPageTransformer { page, position ->
            val absPos = abs(position)
            page.scaleY = 0.85f + (1 - absPos) * 0.15f
            // Kartları birbirine yaklaştırarak Stack efekti verir
            page.translationX = -position * (page.width / 1.5f)
            page.alpha = 0.5f + (1 - absPos)
        }

        // Bottom Nav Kontrolü (Hata riskine karşı null check ekledim)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> true // Buradaki ID menu dosyanla aynı olmalı
                else -> false
            }
        }
    }
}