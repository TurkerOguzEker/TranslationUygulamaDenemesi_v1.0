package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentDownloadsBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.util.LocalLibraryManager

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!

    // Eski DownloadsAdapter yerine efsane BookAdapter'ımızı kullanıyoruz
    private lateinit var adapter: BookAdapter

    // Firebase Bağlantısı ve Dinleyici (Senkronizasyon için)
    private val db = FirebaseFirestore.getInstance()
    private var booksListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // KİTAPLAR DARALDI SORUNUNUN ÇÖZÜMÜ: Tıpkı bitirilenlerdeki gibi 2 sütunlu Grid!
        binding.rvDownloads.layoutManager = GridLayoutManager(requireContext(), 2)

        // DETAY SAYFASI ÇIKMIYOR SORUNUNUN ÇÖZÜMÜ: Tıklanma olayı eklendi
        adapter = BookAdapter(emptyList()) { selectedBook ->
            // Kitaba tıklandığında detay penceresini aç
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetailSheet")
        }
        binding.rvDownloads.adapter = adapter

        startCloudSync()
    }

    override fun onResume() {
        super.onResume()
        startCloudSync()
    }

    // BULUT SENKRONİZASYONU VE YEREL VERİLERİ GÖSTERME
    private fun startCloudSync() {
        val ctx = context ?: return

        booksListener = db.collection("books").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            // 1. Firebase'deki (Buluttaki) tüm güncel kitap ID'lerini alıyoruz
            val cloudBookIds = snapshot.documents.map { it.id }

            // 2. Telefonun hafızasına (Local'e) indirilmiş kitapları alıyoruz
            val localBooks = LocalLibraryManager.getDownloadedBooks(ctx)
            val validLocalBooks = ArrayList<Book>()

            // 3. Karşılaştırma ve Otomatik Temizlik
            for (localBook in localBooks) {
                if (cloudBookIds.contains(localBook.bookId)) {
                    validLocalBooks.add(localBook)
                } else {
                    // Admin webden sildiyse telefondan da sil
                    LocalLibraryManager.deleteBook(ctx, localBook)
                }
            }

            // 4. Görünürlüğü ayarla ve listeyi güncelle
            if (validLocalBooks.isEmpty()) {
                binding.rvDownloads.visibility = View.GONE
                // Eğer fragment_downloads.xml içinde tvEmptyDownloads diye bir ID yoksa hata verebilir.
                // Varsa bunu açabilirsin: binding.tvEmptyDownloads.visibility = View.VISIBLE
            } else {
                binding.rvDownloads.visibility = View.VISIBLE
                // binding.tvEmptyDownloads.visibility = View.GONE

                adapter.updateBooks(validLocalBooks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        booksListener?.remove()
        _binding = null
    }
}