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
    val genre: String = "", // YENİ EKLENEN: Kitabın Türü (Kategorisi)
    val imageUrl: String = "",
    val description: String? = "",
    val chapters: List<Chapter> = ArrayList(),
    val questions: List<Question> = ArrayList(),
    var readCount: Int = 0
) : Serializable