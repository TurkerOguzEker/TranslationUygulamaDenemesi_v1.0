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
import android.widget.FrameLayout
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
    private var progressListener: ListenerRegistration? = null // YENİ: Üst üste binmeyi engellemek için eklendi

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

        startRealtimeTracking(view)
        fetchBooks()
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

    private fun fetchBooks() {
        booksListener = db.collection("books")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val allBooksList = mutableListOf<Book>()

                    for (document in snapshot.documents) {
                        try {
                            val book = document.toObject(Book::class.java)
                            if (book != null) {
                                if (book.bookId.isEmpty()) book.bookId = document.id
                                allBooksList.add(book)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // 1. Son Okunan Kitabı Bul (Okumaya Devam Et Kısmı)
                    findAndShowContinueReading(allBooksList)

                    // 2. Kapsayıcıyı bul ve temizle (A1, A2 Listeleri)
                    val levelsContainer = view?.findViewById<LinearLayout>(R.id.layoutLevelsContainer)

                    if (levelsContainer != null) {
                        levelsContainer.removeAllViews()

                        val groupedBooks = allBooksList.groupBy { it.level }
                        val orderedLevels = listOf("A1", "A2", "B1", "B1+", "B2", "C1", "C2")

                        for (levelCode in orderedLevels) {
                            val booksInLevel = groupedBooks[levelCode]
                            if (!booksInLevel.isNullOrEmpty()) {
                                createLevelSectionView(levelsContainer, levelCode, booksInLevel)
                            }
                        }
                    }
                }
            }
    }

    // SON OKUNAN KİTABI BUL VE TEPEDE GÖSTER
    private fun findAndShowContinueReading(allBooks: List<Book>) {
        val uid = auth.currentUser?.uid ?: return
        val layoutContinueReading = view?.findViewById<LinearLayout>(R.id.layoutContinueReading)
        val flContinueReadingBook = view?.findViewById<FrameLayout>(R.id.flContinueReadingBook)

        if (layoutContinueReading == null || flContinueReadingBook == null) return

        // YENİ: Eski dinleyiciyi sil ki çoklu dinleme hatası (çökme/donma) yapmasın!
        progressListener?.remove()

        progressListener = db.collection("users").document(uid).collection("book_progress")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    layoutContinueReading.visibility = View.GONE
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {

                    // Yüzdesi 100'den küçük olanları al (%0 olanlar da dahil)
                    val inProgressBooks = querySnapshot.documents.filter { doc ->
                        val prog = doc.getLong("progress")?.toInt() ?: 0
                        prog < 100
                    }

                    // En güncel (son okunan) kitabı bul
                    val lastReadDoc = inProgressBooks.maxByOrNull { doc ->
                        doc.getLong("lastReadTimestamp") ?: 0L
                    }

                    if (lastReadDoc != null) {
                        val lastReadBookId = lastReadDoc.id
                        val lastReadBook = allBooks.find { it.bookId == lastReadBookId }

                        if (lastReadBook != null) {
                            layoutContinueReading.visibility = View.VISIBLE
                            flContinueReadingBook.removeAllViews()

                            val inflater = LayoutInflater.from(context)
                            val cardView = inflater.inflate(R.layout.item_continue_reading_card, flContinueReadingBook, false)

                            val ivCover = cardView.findViewById<android.widget.ImageView>(R.id.imgContinueCover)
                            val tvTitle = cardView.findViewById<TextView>(R.id.txtContinueTitle)
                            val tvAuthor = cardView.findViewById<TextView>(R.id.txtContinueAuthor)
                            val pbProgress = cardView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.pbContinueProgressHorizontal)
                            val tvProgress = cardView.findViewById<TextView>(R.id.tvContinueProgress)

                            val currentProgress = lastReadDoc.getLong("progress")?.toInt() ?: 0

                            tvTitle?.text = lastReadBook.title
                            tvAuthor?.text = lastReadBook.author ?: "Bilinmiyor"
                            pbProgress?.progress = currentProgress
                            tvProgress?.text = "%$currentProgress"

                            if (lastReadBook.imageUrl.isNotEmpty() && ivCover != null && context != null) {
                                Glide.with(requireContext()).load(lastReadBook.imageUrl).into(ivCover)
                            }

                            cardView.setOnClickListener {
                                val intent = Intent(requireContext(), BookReaderActivity::class.java)
                                intent.putExtra("BOOK_DATA", lastReadBook)
                                startActivity(intent)
                            }

                            flContinueReadingBook.addView(cardView)
                        } else {
                            layoutContinueReading.visibility = View.GONE
                        }
                    } else {
                        layoutContinueReading.visibility = View.GONE
                    }
                } else {
                    layoutContinueReading.visibility = View.GONE
                }
            }
    }

    private fun formatReadCount(count: Int): String {
        return if (count >= 100) {
            val kValue = count / 1000.0
            val formatted = String.format(java.util.Locale.US, "%.1f", kValue)
            if (formatted.endsWith(".0")) {
                "${formatted.replace(".0", "")}k"
            } else {
                "${formatted}k"
            }
        } else {
            count.toString()
        }
    }

    private fun createLevelSectionView(parent: LinearLayout, levelCode: String, books: List<Book>) {
        val inflater = LayoutInflater.from(context)
        val sectionView = inflater.inflate(R.layout.level_section_layout, parent, false)

        val tvHeader = sectionView.findViewById<TextView>(R.id.tvSectionTitle)
        tvHeader.text = getLevelDisplayName(levelCode)

        val rvLevelBooks = sectionView.findViewById<RecyclerView>(R.id.rvBookList)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvLevelBooks.layoutManager = layoutManager

        val snapHelper = androidx.recyclerview.widget.LinearSnapHelper()
        if (rvLevelBooks.onFlingListener == null) {
            snapHelper.attachToRecyclerView(rvLevelBooks)
        }

        val paddingInPx = (16 * resources.displayMetrics.density).toInt()
        rvLevelBooks.setPadding(paddingInPx, 0, paddingInPx, 0)
        rvLevelBooks.clipToPadding = false

        val adapter = BookAdapter(books) { selectedBook ->
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetailSheet")
        }
        rvLevelBooks.adapter = adapter

        parent.addView(sectionView)
    }

    private fun getLevelDisplayName(levelCode: String): String {
        return when (levelCode) {
            "A1" -> "Elementary (A1)"
            "A2" -> "Pre-Intermediate (A2)"
            "B1" -> "Intermediate (B1)"
            "B1+" -> "Upper-Intermediate (B1+)"
            "B2" -> "Upper-Intermediate (B2)"
            "C1" -> "Advanced (C1)"
            "C2" -> "Proficient (C2)"
            else -> "Diğer Kitaplar ($levelCode)"
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
        progressListener?.remove()
    }
}