package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import turkeroguz.eker.translationuygulamadenemesi_v10.BookReaderActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.util.LocalLibraryManager

class BookDetailBottomSheet(private val book: Book) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var isFavorite = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_book_detail_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivCover = view.findViewById<ImageView>(R.id.ivDetailCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvAuthor = view.findViewById<TextView>(R.id.tvDetailAuthor)
        val tvDesc = view.findViewById<TextView>(R.id.tvDetailDescription)
        val chipLevel = view.findViewById<Chip>(R.id.chipDetailLevel)
        val btnFavorite = view.findViewById<MaterialButton>(R.id.btnAddToFavorites)
        val btnReadNow = view.findViewById<MaterialButton>(R.id.btnReadNow)
        val btnClose = view.findViewById<Button>(R.id.btnCloseDetail)

        tvTitle.text = book.title
        tvAuthor.text = book.author ?: "Bilinmeyen Yazar"
        chipLevel.text = if (book.level.isNotEmpty()) book.level else "Genel"
        tvDesc.text = if (!book.description.isNullOrEmpty()) book.description else "Bu kitap için açıklama girilmemiş."

        if (book.imageUrl.isNotEmpty()) {
            Glide.with(this).load(book.imageUrl).into(ivCover)
        }

        checkIfFavorite(btnFavorite)

        btnFavorite.setOnClickListener {
            toggleFavorite(btnFavorite)
        }

        // --- OKUMA VE İNDİRME MANTIĞI ---
        val isDownloaded = LocalLibraryManager.isBookDownloaded(requireContext(), book.bookId)

        if (isDownloaded) {
            btnReadNow.text = "Oku"
            btnReadNow.setIconResource(R.drawable.ic_check)
        } else {
            btnReadNow.text = "İndir ve Oku"
            btnReadNow.setIconResource(R.drawable.ic_download)
        }

        btnReadNow.setOnClickListener {
            if (isDownloaded) {
                openDownloadedBook()
            } else {
                btnReadNow.isEnabled = false
                btnReadNow.text = "İndiriliyor..."

                CoroutineScope(Dispatchers.Main).launch {
                    val success = LocalLibraryManager.downloadAndSaveBook(requireContext(), book)

                    if (success) {
                        Toast.makeText(context, "İndirme Tamamlandı!", Toast.LENGTH_SHORT).show()
                        openDownloadedBook()
                    } else {
                        btnReadNow.isEnabled = true
                        btnReadNow.text = "Hata! Tekrar Dene"
                        Toast.makeText(context, "İndirme başarısız oldu.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnClose.setOnClickListener { dismiss() }
    }

    private fun openDownloadedBook() {
        val downloadedBooks = LocalLibraryManager.getDownloadedBooks(requireContext())
        val localBook = downloadedBooks.find { it.bookId == book.bookId } ?: book

        val intent = Intent(requireContext(), BookReaderActivity::class.java)
        intent.putExtra("BOOK_DATA", localBook)
        startActivity(intent)

        // İndirilenler sekmesine git (Index 0)
        navigateToMyBooks(0)
        dismiss()
    }

    // --- GARANTİ ÇALIŞAN NAVİGASYON ---
    private fun navigateToMyBooks(tabIndex: Int) {
        try {
            // 1. Önce MyBooksFragment'a not bırakıyoruz: "Açıldığında şu sekmeye git"
            MyBooksFragment.pendingTabIndex = tabIndex

            // 2. Sonra Ana Menüden "Kitaplarım"a tıklatıyoruz.
            val navView = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
            navView?.selectedItemId = R.id.nav_my_books

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkIfFavorite(btn: MaterialButton) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("favorites").document(book.bookId)
            .get()
            .addOnSuccessListener { document ->
                isFavorite = document.exists()
                updateFavoriteButtonUI(btn)
            }
    }

    private fun toggleFavorite(btn: MaterialButton) {
        val uid = auth.currentUser?.uid ?: return
        val favRef = db.collection("users").document(uid).collection("favorites").document(book.bookId)
        val safeContext = requireContext().applicationContext

        btn.isEnabled = false

        if (isFavorite) {
            // MESAJI ANINDA GÖSTER
            Toast.makeText(safeContext, "Kitap favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()

            favRef.delete().addOnSuccessListener {
                isFavorite = false
                updateFavoriteButtonUI(btn)
                btn.isEnabled = true
            }
        } else {
            // MESAJI ANINDA GÖSTER
            Toast.makeText(safeContext, "Kitap favorilere eklendi! ⭐", Toast.LENGTH_SHORT).show()

            favRef.set(book).addOnSuccessListener {
                isFavorite = true
                updateFavoriteButtonUI(btn)
                btn.isEnabled = true

                // --- FAVORİLER SEKMESİNE GİT ---
                dismiss() // Pencereyi kapat
                navigateToMyBooks(2) // 2 Numaralı Sekme (Favoriler)
            }
        }
    }

    private fun updateFavoriteButtonUI(btn: MaterialButton) {
        if (isFavorite) {
            btn.text = "Favorilerden Çıkar"
            btn.setIconResource(R.drawable.ic_delete)
            btn.setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"))
        } else {
            btn.text = "Favorilere Ekle"
            btn.setIconResource(R.drawable.ic_favorite_star)
            btn.setBackgroundColor(android.graphics.Color.parseColor("#424242"))
        }
    }
}