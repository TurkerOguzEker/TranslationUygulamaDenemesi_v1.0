package turkeroguz.eker.translationuygulamadenemesi_v10.model

import java.io.Serializable

data class Word(
    var wordId: String = "",
    val english: String = "",
    val turkish: String = "",
    val addedAt: Long = 0L,
    var isLearned: Boolean = false
) : Serializable
