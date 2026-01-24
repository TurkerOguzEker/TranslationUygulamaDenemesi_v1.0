package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.FeaturedAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentHomeBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.LevelSectionLayoutBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.FeaturedBook
import kotlin.math.abs

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

        binding.btnNotifications.setOnClickListener {
            showNotificationsDialog()
        }


        // ... diğer kodların (btnProfile, btnNotifications vs.) ...

        val continueReadingCard = binding.continueReadingCard

// Kartın yüksekliğini doğru ölçmek için 'post' bloğu içine alıyoruz
        continueReadingCard.post {
            val originalHeight = continueReadingCard.measuredHeight
            var isCardVisible = true // Animasyonların çakışmasını önlemek için bayrak

            binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                val dy = scrollY - oldScrollY // Değişim miktarı

                // AŞAĞI KAYDIRIYORSAK -> KARTI "EZEREK" KAPAT
                if (dy > 15 && isCardVisible) {
                    isCardVisible = false

                    // Yüksekliği originalHeight'tan 0'a indiren animasyon
                    val animator = android.animation.ValueAnimator.ofInt(originalHeight, 0)
                    animator.addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        // Kartın boyunu anlık olarak güncelle
                        val params = continueReadingCard.layoutParams
                        params.height = value
                        continueReadingCard.layoutParams = params
                        // Aynı anda saydamlaştır (daha şık durur)
                        continueReadingCard.alpha = value.toFloat() / originalHeight
                    }
                    animator.duration = 300 // Animasyon hızı (ms)
                    animator.start()
                }

                // YUKARI KAYDIRIYORSAK -> KARTI TEKRAR AÇ
                else if (dy < -15 && !isCardVisible) {
                    isCardVisible = true

                    // Yüksekliği 0'dan originalHeight'a çıkaran animasyon
                    val animator = android.animation.ValueAnimator.ofInt(0, originalHeight)
                    animator.addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        val params = continueReadingCard.layoutParams
                        params.height = value
                        continueReadingCard.layoutParams = params
                        continueReadingCard.alpha = value.toFloat() / originalHeight
                    }
                    animator.duration = 300
                    animator.start()
                }
            })
        }

// ... return view ...

        return view
    }

    private fun showNotificationsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bildirimler")
            .setMessage("Gösterilecek bildirim yok.")
            .setPositiveButton("Tamam") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(4))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.9f + r * 0.1f
            }
            it.setPageTransformer(compositePageTransformer)
        }
    }
}
