package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentMyBooksBinding

class MyBooksFragment : Fragment() {

    private var _binding: FragmentMyBooksBinding? = null
    private val binding get() = _binding!!

    // --- BU KISIM EKLENDİ: HEDEF SEKME KONTROLÜ İÇİN ---
    companion object {
        // 0: İndirilenler, 1: Bitirilenler, 2: Favoriler
        var pendingTabIndex: Int? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager Adapter Ayarları
        val adapter = turkeroguz.eker.translationuygulamadenemesi_v10.adapter.MyBooksPagerAdapter(this)
        binding.viewPagerBooks.adapter = adapter

        // TabLayout Başlıkları
        TabLayoutMediator(binding.tabLayoutBooks, binding.viewPagerBooks) { tab, position ->
            tab.text = when (position) {
                0 -> "İndirilenler"
                1 -> "Bitirilenler"
                2 -> "Favoriler"
                else -> "Sekme $position"
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        // Fragment ekrana geldiğinde bekleyen bir sekme değişimi var mı kontrol et
        pendingTabIndex?.let { index ->
            // viewPagerBooks hazırsa sekmeyi değiştir
            binding.viewPagerBooks.post {
                binding.viewPagerBooks.currentItem = index
            }
            // İşlem yapıldıktan sonra isteği sıfırla
            pendingTabIndex = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}