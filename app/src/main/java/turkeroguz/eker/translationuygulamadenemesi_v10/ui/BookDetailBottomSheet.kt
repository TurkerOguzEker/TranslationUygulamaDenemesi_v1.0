package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
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
                        showModernToast("İndirme Tamamlandı!", R.drawable.ic_check, "#4CAF50") // Yeşil başarılı mesajı
                        openDownloadedBook()
                    } else {
                        btnReadNow.isEnabled = true
                        btnReadNow.text = "Hata! Tekrar Dene"
                        showModernToast("İndirme başarısız oldu.", R.drawable.ic_close, "#F44336") // Kırmızı hata mesajı
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

        navigateToMyBooks(0)
        dismiss()
    }

    private fun navigateToMyBooks(tabIndex: Int) {
        try {
            turkeroguz.eker.translationuygulamadenemesi_v10.ui.MyBooksFragment.pendingTabIndex = tabIndex
            activity?.findViewById<View>(R.id.btnMyBooks)?.performClick()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkIfFavorite(btn: MaterialButton) {
        val uid = auth.currentUser?.uid ?: return
        if (book.bookId.isEmpty()) return // ID boşsa çökmesin

        db.collection("users").document(uid).collection("favorites").document(book.bookId)
            .get()
            .addOnSuccessListener { document ->
                isFavorite = document.exists()
                updateFavoriteButtonUI(btn)
            }
    }

    private fun toggleFavorite(btn: MaterialButton) {
        val uid = auth.currentUser?.uid ?: return

        if (book.bookId.isEmpty()) {
            showModernToast("Hata: Kitap ID'si bulunamadı!", R.drawable.ic_close, "#F44336")
            return
        }

        val favRef = db.collection("users").document(uid).collection("favorites").document(book.bookId)
        btn.isEnabled = false

        if (isFavorite) {
            showModernToast("Kitap favorilerden çıkarıldı", R.drawable.ic_delete, "#D32F2F")

            favRef.delete().addOnSuccessListener {
                isFavorite = false
                updateFavoriteButtonUI(btn)
                btn.isEnabled = true
            }
        } else {
            showModernToast("Kitap favorilere eklendi!", R.drawable.ic_favorite_star, "#FF9800") // Turuncu/Sarı uyarı rengi

            favRef.set(book).addOnSuccessListener {
                isFavorite = true
                updateFavoriteButtonUI(btn)
                btn.isEnabled = true

                dismiss()
                navigateToMyBooks(2)
            }.addOnFailureListener { e ->
                showModernToast("Kayıt Hatası: ${e.message}", R.drawable.ic_close, "#F44336")
                btn.isEnabled = true
            }
        }
    }

    private fun updateFavoriteButtonUI(btn: MaterialButton) {
        if (isFavorite) {
            btn.text = "Favorilerden Çıkar"
            btn.setIconResource(R.drawable.ic_delete)
            btn.setBackgroundColor(Color.parseColor("#D32F2F"))
        } else {
            btn.text = "Favorilere Ekle"
            btn.setIconResource(R.drawable.ic_favorite_star)
            btn.setBackgroundColor(Color.parseColor("#424242"))
        }
    }

    // --- MODERN VE ÖZELLEŞTİRİLMİŞ UYARI MESAJI (TOAST) FONKSİYONU ---
    private fun showModernToast(message: String, iconResId: Int, colorHex: String = "#424242") {
        val ctx = context ?: return

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(48, 32, 48, 32)
            background = GradientDrawable().apply {
                setColor(Color.parseColor(colorHex))
                cornerRadius = 100f // Tam yuvarlak hap (pill) tasarımı
            }
        }

        val icon = ImageView(ctx).apply {
            setImageResource(iconResId)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(60, 60).apply {
                setMargins(0, 0, 24, 0)
            }
        }

        val text = TextView(ctx).apply {
            this.text = message
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        layout.addView(icon)
        layout.addView(text)

        Toast(ctx).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
}