package turkeroguz.eker.translationuygulamadenemesi_v10.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user",

    // --- DÜZELTME BURADA ---
    // Firebase'e "Bu alanı 'premium' diye değil, 'isPremium' diye oku/yaz" diyoruz.
    @get:PropertyName("isPremium") @set:PropertyName("isPremium")
    var isPremium: Boolean = false,

    val hasPurchasedBefore: Boolean = false,

    @get:PropertyName("isBanned") @set:PropertyName("isBanned")
    var isBanned: Boolean = false,

    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    val registrationDate: Long = 0,
    val lastLoginDate: Long = 0,
    val deviceInfo: String = "Bilinmiyor",
    val storiesStarted: Int = 0,
    val storiesCompleted: Int = 0,
    val totalWordsLearned: Int = 0,
    val totalRevenue: Double = 0.0,
    val streakDays: Int = 0,
    val passwordPlaceholder: String = ""
) {
    constructor() : this("", "", "")
}