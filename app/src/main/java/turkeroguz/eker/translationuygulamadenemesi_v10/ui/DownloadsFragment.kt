package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.DownloadsAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentDownloadsBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.util.LocalLibraryManager

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DownloadsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView için LayoutManager ayarlanıyor
        binding.rvDownloads.layoutManager = LinearLayoutManager(requireContext())

        // Adapter tanımlanıyor ve RecyclerView'a bağlanıyor
        adapter = DownloadsAdapter(
            books = emptyList(),
            onBookClick = { book ->
                // Çevrimdışı Oku: Tıklanan kitabı BookReaderActivity'e gönderiyoruz
                val intent = Intent(requireContext(), BookReaderActivity::class.java)
                intent.putExtra("BOOK_DATA", book)
                startActivity(intent)
            },
            onDeleteClick = { book ->
                // Silme Onayı Dialog'u
                AlertDialog.Builder(requireContext())
                    .setTitle("Kitabı Sil")
                    .setMessage("'${book.title}' cihazdan silinsin mi?")
                    .setPositiveButton("Sil") { _, _ ->
                        LocalLibraryManager.deleteBook(requireContext(), book)
                        loadBooks() // Silme işleminden sonra listeyi yenile
                        Toast.makeText(requireContext(), "Kitap silindi", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        )
        binding.rvDownloads.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Sayfa her açıldığında (veya geri dönüldüğünde) listeyi yenile
        loadBooks()
    }

    private fun loadBooks() {
        // Cihazdaki kitapları çek ve Adapter'ı güncelle
        val books = LocalLibraryManager.getDownloadedBooks(requireContext())
        adapter.updateList(books)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Memory leak (bellek sızıntısı) olmaması için binding'i temizliyoruz
        _binding = null
    }
}