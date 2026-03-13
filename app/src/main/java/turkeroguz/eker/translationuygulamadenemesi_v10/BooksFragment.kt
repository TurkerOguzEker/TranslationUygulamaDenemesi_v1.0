package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.BookDetailBottomSheet
import java.util.Locale

class BooksFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val originalList = ArrayList<Book>()
    private val displayList = ArrayList<Book>()
    private lateinit var bookAdapter: BookAdapter

    // YENİ EKLENDİ: Kitapları dinlemek için Listener
    private var booksListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        // --- NAVBAR'I GÖRÜNÜR YAP ---
        (activity as? MainActivity)?.setBottomNavVisibility(true)

        val rvBooks: RecyclerView = view.findViewById(R.id.rvBooks)
        val etSearch: TextInputEditText = view.findViewById(R.id.etSearch)
        val actvLevel: AutoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)

        rvBooks.layoutManager = GridLayoutManager(context, 2)

        // --- TIKLAMA OLAYI: DETAY PENCERESİNİ AÇ (FIREBASE VERİSİ İLE) ---
        bookAdapter = BookAdapter(displayList) { selectedBook ->
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetailSheet")
        }
        rvBooks.adapter = bookAdapter

        // Dropdown Ayarları
        val levels = arrayOf("Tümü", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        actvLevel.setAdapter(levelAdapter)

        // Verileri Çek
        fetchBooks()

        // Arama Dinleyicisi
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString()
                val currentLevel = actvLevel.text.toString()
                filterCombined(if(currentLevel.isEmpty()) "Tümü" else currentLevel, searchText)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Seviye Dinleyicisi
        actvLevel.setOnItemClickListener { parent, _, position, _ ->
            val selectedLevel = parent.getItemAtPosition(position).toString()
            val currentSearchText = etSearch.text.toString()
            filterCombined(selectedLevel, currentSearchText)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisibility(true)
    }

    // GÜNCELLENDİ: Gerçek Zamanlı (Realtime) Kitap Çekme
    private fun fetchBooks() {
        booksListener = db.collection("books").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Toast.makeText(context, "Veriler güncellenemedi: ${error?.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            originalList.clear()
            displayList.clear()

            for (document in snapshot.documents) {
                val book = document.toObject(Book::class.java)
                if (book != null) {
                    book.bookId = document.id
                    originalList.add(book)
                }
            }

            // Veriler anlık değiştiğinde mevcut arama ve filtreleme kurallarını tekrar uygula
            val currentSearchText = view?.findViewById<TextInputEditText>(R.id.etSearch)?.text.toString()
            val currentLevel = view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)?.text.toString()

            filterCombined(if(currentLevel.isEmpty()) "Tümü" else currentLevel, currentSearchText)
        }
    }

    private fun filterCombined(level: String, query: String) {
        displayList.clear()
        val filtered = originalList.filter { book ->
            val matchLevel = (level == "Tümü" || level == "All" || book.level == level)
            val matchName = if (query.isEmpty()) true else book.title.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            matchLevel && matchName
        }
        displayList.addAll(filtered)
        bookAdapter.notifyDataSetChanged()
    }

    // YENİ EKLENDİ: Sayfa kapanınca dinlemeyi durdur ki RAM yemesin
    override fun onDestroyView() {
        super.onDestroyView()
        booksListener?.remove()
    }
}