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

class EmailSender {

    // NOT: Gmail kullanıyorsanız "App Password" (Uygulama Şifresi) oluşturup buraya girmelisiniz.
    // Normal Gmail şifresi çalışmaz.
    private val senderEmail = "finishingsoftware@gmail.com"
    private val senderPassword = "vkys jghp dzus bkbg" // 16 haneli App Password

    suspend fun sendVerificationCode(recipientEmail: String, code: String): Boolean {
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
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail, "İngilizce Tekrar App"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "E-posta Doğrulama Kodu"
                    setText("Merhaba,\n\nUygulamaya kayıt olmak için doğrulama kodunuz:\n\n$code\n\nBu kodu kimseyle paylaşmayın.")
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                Log.e("EmailSender", "Hata: ${e.message}")
                false
            }
        }
    }
}