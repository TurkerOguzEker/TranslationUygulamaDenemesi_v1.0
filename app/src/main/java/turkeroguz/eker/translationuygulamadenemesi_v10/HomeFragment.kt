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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.BookDetailBottomSheet

class HomeFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var userListener: ListenerRegistration? = null
    private var booksListener: ListenerRegistration? = null

    // Adaptörleri sayfa açılışında tanımlıyoruz
    private lateinit var featuredAdapter: BookAdapter
    private lateinit var allBooksAdapter: BookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisibility(true)

        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)
        loadProfileImage(btnProfile)

        btnProfile.setOnClickListener {
            (activity as? MainActivity)?.showProfileDialog()
        }

        // 1. ADAPTÖRLERİ SADECE BİR KERE BAĞLA (Init)
        setupRecyclerViews(view)

        startRealtimeTracking(view)
        fetchBooks()
    }

    // RecyclerView'ları bir kez kuran fonksiyon
    private fun setupRecyclerViews(view: View) {
        val rvFeatured = view.findViewById<RecyclerView>(R.id.rvFeaturedBooks)
        val rvAll = view.findViewById<RecyclerView>(R.id.rvAllBooks)

        // Öne Çıkanlar Adaptörü Kurulumu
        featuredAdapter = BookAdapter(emptyList()) { selectedBook ->
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetailSheet")
        }
        rvFeatured?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFeatured?.adapter = featuredAdapter

        // Tüm Kitaplar Adaptörü Kurulumu
        allBooksAdapter = BookAdapter(emptyList()) { selectedBook ->
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetailSheet")
        }
        rvAll?.layoutManager = LinearLayoutManager(context)
        rvAll?.adapter = allBooksAdapter
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisibility(true)
    }

    private fun startRealtimeTracking(view: View) {
        val layoutPremium = view.findViewById<LinearLayout>(R.id.layoutPremiumBadge)
        val tvAdmin = view.findViewById<TextView>(R.id.tvAdminBadge)
        val currentUser = auth.currentUser

        if (currentUser != null) {
            userListener = db.collection("users").document(currentUser.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        val isPremium = doc.getBoolean("isPremium") ?: false
                        layoutPremium?.visibility = if (isPremium) View.VISIBLE else View.GONE

                        val role = doc.getString("role")
                        tvAdmin?.visibility = if (role == "admin") View.VISIBLE else View.GONE
                    }
                }
        } else {
            layoutPremium?.visibility = View.GONE
            tvAdmin?.visibility = View.GONE
        }
    }

    // GÜNCELLENDİ: Hataları engelleyen Kurşun Geçirmez Kitap Çekme Sistemi
    private fun fetchBooks() {
        booksListener = db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                // Eski listeyi clear() yapmak yerine HER SEFERİNDE TERTEMİZ BİR LİSTE oluşturuyoruz
                val safeBookList = mutableListOf<Book>()

                for (document in snapshot.documents) {
                    try {
                        val book = document.toObject(Book::class.java)
                        if (book != null) {
                            if (book.bookId.isEmpty()) book.bookId = document.id // Garanti olsun
                            safeBookList.add(book)
                        }
                    } catch (e: Exception) {
                        // Eğer web'den eklenen bir kitap formatı hatalıysa,
                        // sistemi çökertmeden o kitabı atlar ve diğerlerini göstermeye devam eder.
                        e.printStackTrace()
                    }
                }

                // Adapter'lere eski listenin üstüne yazmak yerine tamamen YENİ LİSTEYİ veriyoruz
                val finalBooks = safeBookList.toList()
                allBooksAdapter.updateBooks(finalBooks)

                // Öne Çıkanları Güncelle (İlk 5 kitap)
                val featuredList = finalBooks.take(5)
                featuredAdapter.updateBooks(featuredList)
            }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        booksListener?.remove()
    }
}