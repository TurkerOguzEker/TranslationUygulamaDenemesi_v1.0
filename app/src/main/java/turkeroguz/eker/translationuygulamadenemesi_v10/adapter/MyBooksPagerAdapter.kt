package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.DownloadsFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.FavoritesFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.FinishedFragment

// Eğer fragment'ların ui klasöründeyse (örn: ui.DownloadsFragment), importları ona göre düzeltmen gerekebilir.

class MyBooksPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Toplam sekme sayımız (İndirilenler, Bitirilenler, Favoriler)
    override fun getItemCount(): Int {
        return 3
    }

    // Hangi sekmede hangi Fragment'ın açılacağını belirliyoruz
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DownloadsFragment() // 1. Sekme: İndirilenler
            1 -> FinishedFragment()  // 2. Sekme: Bitirilenler
            2 -> FavoritesFragment() // 3. Sekme: Favoriler
            else -> DownloadsFragment()
        }
    }
}