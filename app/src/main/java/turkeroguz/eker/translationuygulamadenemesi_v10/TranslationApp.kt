package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class TranslationApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Firebase Firestore Offline Persistence ayarı
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        Firebase.firestore.firestoreSettings = settings
    }
}
