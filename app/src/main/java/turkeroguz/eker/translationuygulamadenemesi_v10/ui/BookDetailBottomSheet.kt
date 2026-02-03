package turkeroguz.eker.translationuygulamadenemesi_v10.ui

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

        // Görünümleri Bağla
        val ivCover = view.findViewById<ImageView>(R.id.ivDetailCover)
        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvAuthor = view.findViewById<TextView>(R.id.tvDetailAuthor)
        val tvDesc = view.findViewById<TextView>(R.id.tvDetailDescription)
        val chipLevel = view.findViewById<Chip>(R.id.chipDetailLevel)
        val btnFavorite = view.findViewById<MaterialButton>(R.id.btnAddToFavorites)
        val btnReadNow = view.findViewById<MaterialButton>(R.id.btnReadNow)
        val btnClose = view.findViewById<Button>(R.id.btnCloseDetail)

        // Verileri Doldur
        tvTitle.text = book.title
        tvAuthor.text = book.author ?: "Bilinmeyen Yazar"
        chipLevel.text = if (book.level.isNotEmpty()) book.level else "Genel"
        tvDesc.text = if (!book.description.isNullOrEmpty()) book.description else "Bu kitap için açıklama girilmemiş."

        // Resmi Yükle
        if (book.imageUrl.isNotEmpty()) {
            Glide.with(this).load(book.imageUrl).into(ivCover)
        }

        // Favori Durumunu Kontrol Et
        checkIfFavorite(btnFavorite)

        // Favori Ekle/Çıkar
        btnFavorite.setOnClickListener {
            toggleFavorite(btnFavorite)
        }

        // --- GÜNCELLENMİŞ İNDİRME VE OKUMA MANTIĞI ---

        // 1. Önce kitabın indirilip indirilmediğini kontrol et
        val isDownloaded = LocalLibraryManager.isBookDownloaded(requireContext(), book.bookId)

        // 2. Butonun şeklini buna göre ayarla
        if (isDownloaded) {
            btnReadNow.text = "Kütüphanemde Aç"
            btnReadNow.setIconResource(R.drawable.ic_check) // (Varsa) Tik işareti koy
        } else {
            btnReadNow.text = "İndir ve Oku"
            btnReadNow.setIconResource(R.drawable.ic_download) // (Varsa) İndirme ikonu koy
        }

        // 3. Tıklama İşlemi
        btnReadNow.setOnClickListener {

            if (isDownloaded) {
                // Zaten inmişse direkt kütüphaneye git
                dismiss() // Pencereyi kapat
                navigateToMyBooks()
            } else {
                // İndirme işlemini başlat
                btnReadNow.isEnabled = false // Tıklamayı engelle
                btnReadNow.text = "İndiriliyor..."

                CoroutineScope(Dispatchers.Main).launch {
                    val success = LocalLibraryManager.downloadAndSaveBook(requireContext(), book)

                    if (success) {
                        Toast.makeText(context, "İndirme Tamamlandı! Kitaplarıma gidiliyor...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        navigateToMyBooks()
                    } else {
                        btnReadNow.isEnabled = true
                        btnReadNow.text = "Hata! Tekrar Dene"
                        Toast.makeText(context, "İndirme başarısız oldu. İnternetinizi kontrol edin.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnClose.setOnClickListener { dismiss() }
    }

    // --- YARDIMCI FONKSİYON: Kitaplarım Sekmesine Git ---
    private fun navigateToMyBooks() {
        // MainActivity'deki alt menüyü (BottomNavigation) buluyoruz
        // NOT: Eğer activity_main.xml içindeki ID farklıysa (örn: bottomNavigationView) burayı değiştir!
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)

        if (bottomNav != null) {
            bottomNav.selectedItemId = R.id.nav_my_books // Kitaplarım sekmesini seçtir
        } else {
            // Eğer ID bulunamazsa normal okuma ekranını açmayı dene (Yedek plan)
            val intent = android.content.Intent(requireContext(), turkeroguz.eker.translationuygulamadenemesi_v10.BookReaderActivity::class.java)
            intent.putExtra("BOOK_DATA", book)
            startActivity(intent)
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

        btn.isEnabled = false

        if (isFavorite) {
            favRef.delete().addOnSuccessListener {
                isFavorite = false
                updateFavoriteButtonUI(btn)
                Toast.makeText(context, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()
                btn.isEnabled = true
            }
        } else {
            favRef.set(book).addOnSuccessListener {
                isFavorite = true
                updateFavoriteButtonUI(btn)
                Toast.makeText(context, "Favorilere eklendi ❤️", Toast.LENGTH_SHORT).show()
                btn.isEnabled = true
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