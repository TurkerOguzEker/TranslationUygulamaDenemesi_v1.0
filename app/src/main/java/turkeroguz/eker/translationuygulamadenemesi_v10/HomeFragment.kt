package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.FeaturedAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentHomeBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.LevelSectionLayoutBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.FeaturedBook

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val featuredList = listOf(
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background)
        )

        setupLevelSections(featuredList)

        binding.btnProfile.setOnClickListener {
            (activity as? MainActivity)?.showProfileDialog()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLevelSections(featuredList: List<FeaturedBook>) {
        setupLevelSection(binding.sectionA1, getString(R.string.level_a1), featuredList)
        setupLevelSection(binding.sectionA2, getString(R.string.level_a2), featuredList)
        setupLevelSection(binding.sectionB1, getString(R.string.level_b1), featuredList)
        setupLevelSection(binding.sectionB1Plus, getString(R.string.level_b1_plus), featuredList)
        setupLevelSection(binding.sectionB2, getString(R.string.level_b2), featuredList)
        setupLevelSection(binding.sectionC1, getString(R.string.level_c1), featuredList)
        setupLevelSection(binding.sectionC2, getString(R.string.level_c2), featuredList)
    }

    private fun setupLevelSection(sectionBinding: LevelSectionLayoutBinding, title: String, books: List<FeaturedBook>) {
        sectionBinding.txtLevelTitle.text = title
        val viewPager = sectionBinding.viewPagerBooks

        viewPager.let {
            it.adapter = FeaturedAdapter(books)
            it.clipToPadding = false
            it.clipChildren = false
            it.offscreenPageLimit = 3

            val marginInDp = 10
            val px = (marginInDp * resources.displayMetrics.density).toInt()
            it.setPadding(px, 0, px, 0)

            it.setPageTransformer { page, position ->
                val absPos = kotlin.math.abs(position)

                val scale = 0.9f + (1 - absPos) * 0.1f
                page.scaleY = scale
                page.scaleX = scale

                page.translationX = -position * (px * 0.8f)
            }
        }
    }
}
