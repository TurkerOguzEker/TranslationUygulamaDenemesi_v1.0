package turkeroguz.eker.translationuygulamadenemesi_v10.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",

    // Çökme sorununu önlemek için Long varsayılan değer
    val registrationDate: Long = 0,
    val lastLoginDate: Long = 0,

    val role: String = "user",

    // --- DÜZELTME BURADA ---
    // Firebase'e "isPremium" alanını tam olarak bu isimle okumasını söylüyoruz.
    // Böylece "premium" alanına bakıp hata yapmaz.
    @get:PropertyName("isPremium") @set:PropertyName("isPremium")
    var isPremium: Boolean = false,

    @get:PropertyName("isBanned") @set:PropertyName("isBanned")
    var isBanned: Boolean = false,

    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    val streakDays: Int = 0,
    val totalWordsLearned: Int = 0,
    val totalRevenue: Double = 0.0,
    val storiesStarted: Int = 0,
    val storiesCompleted: Int = 0,
    val hasPurchasedBefore: Boolean = false,
    val deviceInfo: String = "Bilinmiyor",
    val passwordPlaceholder: String = ""
)