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

    private val db = FirebaseFirestore.getInstance()
    private val bookList = ArrayList<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Profil Butonu ve Resmi Ayarla (Az önce yaptığımız kısım)
        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.showProfileDialog()
        }
        loadProfileImage(btnProfile)

        // 2. Kitapları Getir ve Listele (Kaybolan kısım geri eklendi)
        fetchBooks(view)
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

                // Kitapları seviyelerine göre ayır ve ilgili listelere gönder
                setupSection(view, R.id.sectionA1, "A1 Seviyesi", "A1")
                setupSection(view, R.id.sectionA2, "A2 Seviyesi", "A2")
                setupSection(view, R.id.sectionB1, "B1 Seviyesi", "B1")
                setupSection(view, R.id.sectionB1Plus, "B1+ Seviyesi", "B1+")
                setupSection(view, R.id.sectionB2, "B2 Seviyesi", "B2")
                setupSection(view, R.id.sectionC1, "C1 Seviyesi", "C1")
                setupSection(view, R.id.sectionC2, "C2 Seviyesi", "C2")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Kitaplar yüklenemedi: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // Belirli bir seviyedeki kitapları filtreleyip ilgili RecyclerView'a bağlar
    private fun setupSection(view: View, sectionId: Int, title: String, level: String) {
        val sectionView = view.findViewById<View>(sectionId)
        val tvTitle = sectionView.findViewById<TextView>(R.id.tvSectionTitle)
        val recyclerView = sectionView.findViewById<RecyclerView>(R.id.rvBookList)

        // Bölüm başlığını ayarla
        tvTitle.text = title

        // Sadece bu seviyeye ait kitapları filtrele
        val filteredList = bookList.filter { it.level == level }

        if (filteredList.isNotEmpty()) {
            // Eğer bu seviyede kitap varsa listeyi göster
            sectionView.visibility = View.VISIBLE

            // YATAY KAYDIRMA (Animasyon hissi veren kısım burası)
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            // Adapter'ı bağla
            val adapter = BookAdapter(filteredList) { selectedBook ->
                // Kitaba tıklanınca detay sayfasına git (veya okuma sayfasına)
                // Buradaki kod, BookAdapter'ınızın tıklama mantığına göre değişebilir
                Toast.makeText(context, "${selectedBook.title} seçildi", Toast.LENGTH_SHORT).show()

                // Örnek: Detay sayfasına yönlendirme (Eğer varsa)
                /*
                val intent = Intent(context, BookDetailActivity::class.java)
                intent.putExtra("bookId", selectedBook.bookId)
                startActivity(intent)
                */
            }
            recyclerView.adapter = adapter
        } else {
            // Bu seviyede kitap yoksa o bölümü gizle
            sectionView.visibility = View.GONE
        }
    }

    // --- Profil Resmi Fonksiyonları (Aynı kalıyor) ---
    private fun loadProfileImage(imageView: ImageButton) {
        val user = FirebaseAuth.getInstance().currentUser
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