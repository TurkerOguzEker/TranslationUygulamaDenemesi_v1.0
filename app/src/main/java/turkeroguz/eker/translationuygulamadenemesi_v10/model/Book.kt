package turkeroguz.eker.translationuygulamadenemesi_v10.model

import java.io.Serializable

data class Chapter(
    val chapterTitle: String = "",
    val chapterText: String = "",
    val chapterImageUrl: String = ""
) : Serializable

data class Book(
    var bookId: String = "",
    val title: String = "",
    val author: String? = "",
    val level: String = "",
    val imageUrl: String = "",
    val description: String? = "",

    // PDF linkleri gitti, yerine metin bölümleri (Chapter) geldi
    val chapters: List<Chapter> = ArrayList(),

    val questions: List<Question> = ArrayList(),

    // YENİ EKLENEN: Kitabın kaç kişi tarafından okunduğunu tutacak değişken
    var readCount: Int = 0
) : Serializable