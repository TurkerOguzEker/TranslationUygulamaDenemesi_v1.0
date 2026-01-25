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

        val continueReadingCard = binding.continueReadingCard

        continueReadingCard.post {
            val originalHeight = continueReadingCard.measuredHeight
            var isCardVisible = true

            binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                val dy = scrollY - oldScrollY

                if (dy > 15 && isCardVisible) {
                    isCardVisible = false
                    val animator = android.animation.ValueAnimator.ofInt(originalHeight, 0)
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
                else if (dy < -15 && !isCardVisible) {
                    isCardVisible = true
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

    // --- BURASI GÜNCELLENDİ ---
    private fun setupLevelSection(sectionBinding: LevelSectionLayoutBinding, title: String, books: List<FeaturedBook>) {
        sectionBinding.txtLevelTitle.text = title
        val viewPager = sectionBinding.viewPagerBooks

        viewPager.apply {
            adapter = FeaturedAdapter(books)
            // Yanlardaki öğelerin görünmesi için bu iki ayar şart:
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3

            // --- YENİ EKLENEN KISIM: DİNAMİK PADDING HESAPLAMA ---

            // 1. Ekran genişliğini alıyoruz
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // 2. Bir kitabın kapladığı tahmini genişliği belirliyoruz.
            // xml'de kartın 165dp, sağ-sol boşluklar dahil yaklaşık 180-190dp idealdir.
            // Bu değeri artırırsan kitaplar birbirinden uzaklaşır, azaltırsan yaklaşır.
            val itemWidth = (180 * displayMetrics.density).toInt()

            // 3. Ortadaki kitabı merkeze almak için gereken kenar boşluğunu hesaplıyoruz.
            // Formül: (Ekran Genişliği - Kitap Genişliği) / 2
            val padding = (screenWidth - itemWidth) / 2

            // 4. Hesaplanan boşluğu ViewPager'a uyguluyoruz
            setPadding(padding, 0, padding, 0)

            // -----------------------------------------------------

            val compositePageTransformer = CompositePageTransformer()

            // Kitaplar arası ekstra boşluk (Margin) eklemeye gerek yok, padding bunu halletti.
            compositePageTransformer.addTransformer(MarginPageTransformer(0))

            // Yanlardaki kitapları biraz küçültüp şeffaflaştıran animasyon (Carousel efekti)
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f // Yanlar %15 daha küçük
                page.alpha = 0.5f + r * 0.5f    // Yanlar biraz silik (opsiyonel, istemezsen sil)
            }

            setPageTransformer(compositePageTransformer)
        }
    }
}