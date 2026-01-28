package turkeroguz.eker.translationuygulamadenemesi_v10.model

import com.google.firebase.Timestamp

data class ActivityLog(
    val logId: String = "",
    val action: String = "",      // 'actionType' DEĞİL, 'action' kullanıyoruz
    val details: String = "",     // 'description' DEĞİL, 'details' kullanıyoruz
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "info"     // "success", "warning", "error", "info"
)