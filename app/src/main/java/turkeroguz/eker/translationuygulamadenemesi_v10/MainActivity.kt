package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.theme.TranslationUygulamaDenemesi_v10Theme
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ViewPager2'yi bul
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPagerFeatured)

        // Slider için örnek veri listesi
        val featuredList = listOf(
            FeaturedBook("The Adventure Begins", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery of English", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar Secrets", R.drawable.ic_launcher_background)
        )

        // Adapter'ı oluştur ve bağla
        val adapter = FeaturedAdapter(featuredList)
        viewPager.adapter = adapter

        // Kitapların üst üste binme (Stack) efekti
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer { page, position ->
            page.translationX = -120f * position
            page.scaleY = 1 - (0.20f * kotlin.math.abs(position))
            page.alpha = 0.5f + (1 - kotlin.math.abs(position))
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TranslationUygulamaDenemesi_v10Theme {
        Greeting("Android")
    }
}