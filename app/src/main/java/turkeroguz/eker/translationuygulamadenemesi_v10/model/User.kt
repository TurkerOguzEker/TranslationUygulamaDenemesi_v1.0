package turkeroguz.eker.translationuygulamadenemesi_v10.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "", // İsim alanı eklendi
    val passwordPlaceholder: String = "", // Güvenlik notu: Gerçek şifreyi göremezsiniz, sadece adminin atadığı geçici şifreyi tutabiliriz.
    val role: String = "user",
    val isPremium: Boolean = false,
    val hasPurchasedBefore: Boolean = false, // Daha önce alıp iptal mi etti? (Nötr durumu için)
    val lastLogin: Long = 0, // Timestamp

    // --- İSTATİSTİK VERİLERİ ---
    val storiesStarted: Int = 0,
    val storiesCompleted: Int = 0,
    val totalWordsLearned: Int = 0,
    val totalRevenue: Double = 0.0, // Kullanıcıdan kazanılan toplam para
    val streakDays: Int = 0,
    val isBanned: Boolean = false,
    val isDeleted: Boolean = false
)