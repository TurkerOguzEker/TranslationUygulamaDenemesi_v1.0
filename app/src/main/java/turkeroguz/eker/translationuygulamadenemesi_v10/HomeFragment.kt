package turkeroguz.eker.translationuygulamadenemesi_v10

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // XML'deki yuvarlak butonu bul
        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)

        // Tıklayınca MainActivity'deki menüyü aç
        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.showProfileDialog()
        }

        // Resmi Yükle
        loadProfileImage(btnProfile)
    }

    private fun loadProfileImage(imageView: ImageButton) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            if (user.photoUrl != null) {
                // Varsa profil fotoğrafını yükle
                try {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .circleCrop()
                        .into(imageView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // Yoksa harften resim yap
                val email = user.email ?: ""
                val nameInitial = if (email.isNotEmpty()) email.first().toString().uppercase() else "?"

                val letterBitmap = createProfileBitmap(nameInitial)

                Glide.with(this)
                    .load(letterBitmap)
                    .circleCrop()
                    .into(imageView)
            }
        }
    }

    private fun createProfileBitmap(text: String): Bitmap {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        paint.color = Color.parseColor("#5C6BC0") // Mavi arka plan
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = Color.WHITE
        paint.textSize = 100f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        val xPos = (canvas.width / 2).toFloat()
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)

        canvas.drawText(text, xPos, yPos, paint)

        return bitmap
    }
}