package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentMyBooksBinding

class MyBooksFragment : Fragment() {

    private var _binding: FragmentMyBooksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        viewPager.adapter = MyBooksPagerAdapter(this)

        // Sekme Başlıklarını ve İkonlarını Ayarla
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> { // ARTIK İLK SIRADA: İndirilenler
                    tab.text = getString(R.string.downloads) // "İndirilenler"
                    tab.setIcon(R.drawable.ic_download)
                }
                1 -> { // ORTA: Bitirilenler (Aynı kaldı)
                    tab.text = getString(R.string.finished) // "Bitirilenler"
                    tab.setIcon(R.drawable.ic_finished_flag)
                }
                else -> { // SON: Favoriler
                    tab.text = getString(R.string.favorites) // "Favoriler"
                    tab.setIcon(R.drawable.ic_favorite_star)
                }
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Fragment Sıralamasını Ayarlayan Adapter
    private inner class MyBooksPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DownloadsFragment() // İlk açılışta burası gelecek
                1 -> FinishedFragment()
                else -> FavoritesFragment() // Sona kaydırdık
            }
        }
    }
}