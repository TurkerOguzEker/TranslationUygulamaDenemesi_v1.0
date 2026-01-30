package turkeroguz.eker.translationuygulamadenemesi_v10.model

data class Book(
    var bookId: String = "",
    val title: String = "",
    val author: String = "",
    val level: String = "",
    val imageUrl: String = "",
    val pdfUrl: String = "", // Buradaki virgül muhtemelen eksikti

    // Yeni eklenen alan
    val description: String = ""
)