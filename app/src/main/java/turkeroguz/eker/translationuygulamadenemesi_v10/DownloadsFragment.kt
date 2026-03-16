package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import turkeroguz.eker.translationuygulamadenemesi_v10.BookReaderActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.DownloadsAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentDownloadsBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.util.LocalLibraryManager
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DownloadsAdapter

    // YENİ: Firebase Bağlantısı ve Dinleyici
    private val db = FirebaseFirestore.getInstance()
    private var booksListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDownloads.layoutManager = LinearLayoutManager(context)

        adapter = DownloadsAdapter(emptyList(),
            onBookClick = { book ->
                val intent = Intent(requireContext(), BookReaderActivity::class.java)
                intent.putExtra("BOOK_DATA", book)
                startActivity(intent)
            },
            onDeleteClick = { book ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Sil")
                    .setMessage("${book.title} cihazdan silinsin mi?")
                    .setPositiveButton("Sil") { _, _ ->
                        LocalLibraryManager.deleteBook(requireContext(), book)
                        startCloudSync() // Silme işleminden sonra listeyi yenile
                        Toast.makeText(context, "Kitap cihazdan silindi", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        )
        binding.rvDownloads.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Sayfa açıldığında sadece yereli okuma, BULUT İLE SENKRONİZE ET
        startCloudSync()
    }

    // EKSİK OLAN MÜKEMMEL FONKSİYON: BULUT SENKRONİZASYONU
    private fun startCloudSync() {
        booksListener = db.collection("books").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            // 1. Firebase'deki (Buluttaki) tüm güncel kitap ID'lerini alıyoruz
            val cloudBookIds = snapshot.documents.map { it.id }

            // 2. Telefonun hafızasına (Local'e) indirilmiş kitapları alıyoruz
            context?.let { ctx ->
                val localBooks = LocalLibraryManager.getDownloadedBooks(ctx)
                val validLocalBooks = ArrayList<Book>()

                // 3. Karşılaştırma ve Otomatik Temizlik yapıyoruz
                for (localBook in localBooks) {
                    // Eğer telefondaki kitap, Firebase'de hala duruyorsa:
                    if (cloudBookIds.contains(localBook.bookId)) {
                        validLocalBooks.add(localBook)
                    }
                    // Eğer telefondaki kitap Firebase'de YOKSA (Admin web'den silmişse):
                    else {
                        LocalLibraryManager.deleteBook(ctx, localBook)
                    }
                }

                // 4. Ekrana SADECE Firebase'de hala var olan kitapları basıyoruz
                adapter.updateList(validLocalBooks)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        booksListener?.remove() // Dinlemeyi kapat
        _binding = null
    }
}