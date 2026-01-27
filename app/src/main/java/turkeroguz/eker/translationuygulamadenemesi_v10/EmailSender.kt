package turkeroguz.eker.translationuygulamadenemesi_v10

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    // Buradaki mail ve şifre sizin girmiş olduğunuz bilgilerdir.
    private const val SENDER_EMAIL = "finishingsoftware@gmail.com"
    private const val SENDER_PASSWORD = "vkys jghp dzus bkbg"

    // İsim parametresi eklendi
    suspend fun sendVerificationCode(recipientEmail: String, userName: String, code: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL, "İngilizce Tekrar App"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "E-posta Doğrulama Kodu"
                    // Kullanıcı ismini de ekledik
                    setText("Merhaba $userName,\n\nUygulamaya kayıt olmak için doğrulama kodunuz:\n\n$code\n\nBu kodu kimseyle paylaşmayın.")
                }

                Transport.send(message)
                Log.d("EmailSender", "Mail başarıyla gönderildi.")
                true
            } catch (e: Exception) {
                Log.e("EmailSender", "Mail Hatası: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
}