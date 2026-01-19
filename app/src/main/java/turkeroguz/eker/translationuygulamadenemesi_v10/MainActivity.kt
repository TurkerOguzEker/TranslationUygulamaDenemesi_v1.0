package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Tasarımı bağla
        setContentView(R.layout.activity_main)

        // 2. ViewPager2 (Slider) Ayarları
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerFeatured)

        // Örnek veri listesi
        val featuredList = listOf(
            FeaturedBook("The Adventure Begins", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery of English", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar Secrets", R.drawable.ic_launcher_background)
        )

        // Adapter bağlantısı
        viewPager.adapter = FeaturedAdapter(featuredList)

        // 3. ViewPager2 Çökmesini Engelleyen ve Efekt Veren Ayarlar
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.offscreenPageLimit = 3

        // Kenarlardan boşluk bırakarak kartları ortalar (Çökmeyi engeller)
        val offsetPx = (60 * resources.displayMetrics.density).toInt()
        viewPager.setPadding(offsetPx, 0, offsetPx, 0)

        // Sayfa geçiş efekti (Stack Effect)
        viewPager.setPageTransformer { page, position ->
            page.translationX = -30f * position
            page.scaleY = 1 - (0.15f * abs(position))
            page.alpha = 0.5f + (1 - abs(position))
        }

        // 4. Alt Menü Ayarları
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> true
                else -> false
            }
        }
    }
}