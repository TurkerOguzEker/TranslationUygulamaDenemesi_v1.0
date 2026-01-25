package turkeroguz.eker.translationuygulamadenemesi_v10.model

data class Book(
    var bookId: String = "",
    val title: String = "",
    val level: String = "", // Örn: A1, A2
    val imageUrl: String = "", // Firebase'den resim URL'i gelecek
    val pdfUrl: String = ""    // Kitabın PDF linki (opsiyonel)
) {
    // Firebase'in veriyi dönüştürebilmesi için boş constructor zorunludur
    constructor() : this("", "", "", "", "")
}