package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
// Dikkat: .ui kısımları kaldırıldı, çünkü dosyalarınız ana dizinde
import turkeroguz.eker.translationuygulamadenemesi_v10.DownloadsFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.FavoritesFragment
import turkeroguz.eker.translationuygulamadenemesi_v10.FinishedFragment

class MyBooksPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DownloadsFragment()
            1 -> FinishedFragment()
            2 -> FavoritesFragment()
            else -> DownloadsFragment()
        }
    }
}