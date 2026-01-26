package turkeroguz.eker.translationuygulamadenemesi_v10.model

data class ActivityLog(
    val logId: String = "",
    val userId: String = "",
    val actionType: String = "", // "LOGIN", "READ_BOOK", "PURCHASE", "ERROR", "ADMIN_ACTION"
    val description: String = "", // "A1 Kitabı okundu", "Giriş yapıldı (IP: 192...)"
    val timestamp: Long = 0
)