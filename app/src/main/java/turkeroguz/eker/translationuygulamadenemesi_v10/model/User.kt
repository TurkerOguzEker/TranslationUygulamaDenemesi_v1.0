package turkeroguz.eker.translationuygulamadenemesi_v10.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "", // Kullanıcı Adı
    val role: String = "user", // user, author, admin

    // --- DURUM VE ÜYELİK ---
    val isPremium: Boolean = false,
    val hasPurchasedBefore: Boolean = false, // Daha önce satın alım yapmış mı?
    val isBanned: Boolean = false,      // Hesap durumu
    val isDeleted: Boolean = false,     // Soft delete durumu

    // --- ZAMANLAMALAR ---
    val registrationDate: Long = 0,     // Kayıt tarihi (Timestamp)
    val lastLoginDate: Long = 0,        // Son giriş (Timestamp)
    val deviceInfo: String = "Bilinmiyor", // Örn: "Samsung S21"

    // --- İSTATİSTİKLER ---
    val storiesStarted: Int = 0,        // Başlanan hikaye sayısı
    val storiesCompleted: Int = 0,      // Bitirilen hikaye sayısı
    val totalWordsLearned: Int = 0,     // Öğrenilen kelime
    val totalRevenue: Double = 0.0,     // Toplam harcama
    val streakDays: Int = 0,            // Seri gün sayısı

    val passwordPlaceholder: String = "" // Admin notu için (Gerçek şifre değil)
) {
    // Firebase'in boş constructor ihtiyacı için
    constructor() : this("", "", "")
}