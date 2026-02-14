package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.MyBooksPagerAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentMyBooksBinding

class MyBooksFragment : Fragment() {

    private var _binding: FragmentMyBooksBinding? = null
    private val binding get() = _binding!!

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
        val adapter = MyBooksPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // TabLayout Başlıkları
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
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
            // viewPager hazırsa sekmeyi değiştir
            binding.viewPager.post {
                binding.viewPager.currentItem = index
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