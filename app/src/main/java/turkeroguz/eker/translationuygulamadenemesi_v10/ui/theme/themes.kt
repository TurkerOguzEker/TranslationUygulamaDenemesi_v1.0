package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tasarımı bağlar
        setContentView(R.layout.activity_main)

        // Alt menüyü tanımlar
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> true
                R.id.nav_word -> true
                else -> false
            }
        }
    }
}