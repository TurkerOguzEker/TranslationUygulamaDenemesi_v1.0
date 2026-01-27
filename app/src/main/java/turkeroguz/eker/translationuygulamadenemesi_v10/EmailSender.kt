package turkeroguz.eker.translationuygulamadenemesi_v10.util

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

    // GÜVENLİK UYARISI: Gerçek projelerde bu bilgileri backend'de tutmalısın.
    // Gmail > Hesabım > Güvenlik > 2 Adımlı Doğrulama > Uygulama Şifreleri kısmından şifre al.
    private const val SENDER_EMAIL = "seninmailadresin@gmail.com"
    private const val SENDER_PASSWORD = "xxxx yyyy zzzz aaaa" // 16 haneli uygulama şifresi

    suspend fun sendEmail(recipientEmail: String, subject: String, messageBody: String): Boolean {
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
                    setFrom(InternetAddress(SENDER_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    setSubject(subject)
                    setText(messageBody)
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}