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
import turkeroguz.eker.translationuygulamadenemesi_v10.BookReaderActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.DownloadsAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentDownloadsBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.util.LocalLibraryManager

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DownloadsAdapter

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
                        loadBooks()
                        Toast.makeText(context, "Kitap silindi", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        )
        binding.rvDownloads.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }

    private fun loadBooks() {
        context?.let { ctx ->
            val books = LocalLibraryManager.getDownloadedBooks(ctx)
            adapter.updateList(books)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}