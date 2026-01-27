package turkeroguz.eker.translationuygulamadenemesi_v10

import android.content.Intent
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class HomeFragment : Fragment() {

    // Firebase ve Liste Değişkenleri (Tek seferde tanımlanmalı)
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val bookList = ArrayList<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // XML Dosyasını Şişir
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Profil Resmini Yükle
        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)
        loadProfileImage(btnProfile)

        // Profil butonuna tıklanınca MainActivity'deki dialog açılsın
        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.showProfileDialog()
        }

        // 2. Premium Kontrolü ve Gösterimi
        checkPremiumStatus(view)

        // 3. Kitapları Çek ve Listele
        fetchBooks(view)
    }

    private fun checkPremiumStatus(view: View) {
        val layoutPremiumBadge = view.findViewById<LinearLayout>(R.id.layoutPremiumBadge)
        val currentUser = auth.currentUser

        if (currentUser != null && layoutPremiumBadge != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val isPremium = document.getBoolean("isPremium") == true
                        if (isPremium) {
                            layoutPremiumBadge.visibility = View.VISIBLE
                        } else {
                            layoutPremiumBadge.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener {
                    // Hata olursa varsayılan olarak gizle
                    layoutPremiumBadge.visibility = View.GONE
                }
        }
    }

    private fun fetchBooks(view: View) {
        // Tüm kitapları "books" koleksiyonundan çek
        db.collection("books")
            .get()
            .addOnSuccessListener { result ->
                bookList.clear()
                for (document in result) {
                    // Firestore verisini Book modeline çevir
                    val book = document.toObject(Book::class.java)
                    // Belge ID'sini modele ekle (tıklama işlemleri için gerekebilir)
                    book.bookId = document.id
                    bookList.add(book)
                }

                // NOT: XML'inizde bu ID'lere (sectionA1 vb.) sahip 'include' veya 'LinearLayout' yapıları olmalı.
                // Eğer yoksa "setupSection" çalışırken hata alırsınız.
                // Şimdilik örnek olarak ekliyorum, XML'inizde yoksa bu satırları kapatın veya XML'i güncelleyin.

                // --- KİTAP LİSTELEME MANTIĞI ---
                // Eğer "layout_book_section" gibi bir yapı kullanıyorsanız ID'ler XML ile uyuşmalı.
                // Şimdilik geçici olarak ana listede gösteriyoruz:

                // ÖRNEK: Öne Çıkanlar (RV varsa)
                val rvFeatured = view.findViewById<RecyclerView>(R.id.rvFeaturedBooks)
                if (rvFeatured != null && bookList.isNotEmpty()) {
                    rvFeatured.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    rvFeatured.adapter = BookAdapter(bookList.take(5)) { selectedBook ->
                        Toast.makeText(context, "${selectedBook.title} seçildi", Toast.LENGTH_SHORT).show()
                    }
                }

                // ÖRNEK: Tüm Kitaplar (RV varsa)
                val rvAll = view.findViewById<RecyclerView>(R.id.rvAllBooks)
                if (rvAll != null) {
                    rvAll.layoutManager = LinearLayoutManager(context) // Dikey
                    rvAll.adapter = BookAdapter(bookList) { selectedBook ->
                        Toast.makeText(context, "${selectedBook.title} seçildi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Kitaplar yüklenemedi: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // --- Profil Resmi Fonksiyonları ---
    private fun loadProfileImage(imageView: ImageButton) {
        val user = auth.currentUser
        if (user != null) {
            if (user.photoUrl != null) {
                try {
                    Glide.with(this).load(user.photoUrl).circleCrop().into(imageView)
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                try {
                    val email = user.email ?: ""
                    val initial = if (email.isNotEmpty()) email.first().toString().uppercase() else "?"
                    val letterBitmap = createProfileBitmap(initial)
                    Glide.with(this).load(letterBitmap).circleCrop().into(imageView)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    private fun createProfileBitmap(text: String): Bitmap {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.parseColor("#5C6BC0")
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