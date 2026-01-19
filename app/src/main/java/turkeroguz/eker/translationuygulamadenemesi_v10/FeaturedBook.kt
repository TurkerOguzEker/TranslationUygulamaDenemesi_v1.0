package turkeroguz.eker.translationuygulamadenemesi_v10

/**
 * Slider'da görünecek kitapların verilerini tutan model sınıfı.
 */
data class FeaturedBook(
    val title: String,    // Kitabın adı
    val imageRes: Int     // drawable klasöründeki resmin ID'si (Örn: R.drawable.kitap_kapak)
)