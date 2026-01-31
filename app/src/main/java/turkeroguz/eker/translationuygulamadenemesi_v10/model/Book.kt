package turkeroguz.eker.translationuygulamadenemesi_v10.model

import java.io.Serializable

data class Book(
    var bookId: String = "",
    val title: String = "",
    val author: String? = "",
    val level: String = "",
    val imageUrl: String = "",
    val description: String? = "",

    // Hikayeler hala PDF linki olarak kalabilir (veya metin, sen bilirsin)
    val storyUrls: List<String> = ArrayList(),

    // YENİ: Sorular artık PDF linki değil, Question nesnesi listesi
    val questions: List<Question> = ArrayList()
) : Serializable