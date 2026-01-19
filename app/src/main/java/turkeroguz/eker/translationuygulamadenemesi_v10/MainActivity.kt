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

        // Tasarımı bağla
        setContentView(R.layout.activity_main)

        // Alt menüyü bul (ID: bottom_navigation)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Menü dosyasındaki ID'ler ile birebir aynı olmalı
                R.id.nav_books -> {
                    true
                }
                R.id.nav_word -> {
                    true
                }
                R.id.nav_audio -> {
                    true
                }
                R.id.nav_download -> {
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
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