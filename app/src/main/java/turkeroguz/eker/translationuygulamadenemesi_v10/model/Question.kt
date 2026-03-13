package turkeroguz.eker.translationuygulamadenemesi_v10.model

import java.io.Serializable

data class Question(
    val questionText: String = "",           // Soru metni (HATA VEREN KISIM BURASIYDI, EKLENDİ)
    val options: List<String> = ArrayList(), // Şıklar: ["A", "B", "C", "D"]
    val correctOptionIndex: Int = 0          // Doğru şıkkın sırası (0=A, 1=B)
) : Serializable