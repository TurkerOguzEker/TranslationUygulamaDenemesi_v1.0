package turkeroguz.eker.translationuygulamadenemesi_v10.model

import java.io.Serializable

data class Question(
    var questionPdfUrl: String = "",  // DEĞİŞTİ: Artık metin değil PDF Linki
    var options: List<String> = listOf(), // Şıklar buton metni olarak kalıyor
    var correctOptionIndex: Int = 0
) : Serializable