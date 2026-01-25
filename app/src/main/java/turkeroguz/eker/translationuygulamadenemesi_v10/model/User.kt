package turkeroguz.eker.translationuygulamadenemesi_v10.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "user" // Varsayılan rol: "user". Diğerleri: "admin", "author"
)