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
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.BookDetailBottomSheet // EKLENDİ: Detay penceresi importu
import java.util.Locale

class BooksFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val originalList = ArrayList<Book>()
    private val displayList = ArrayList<Book>()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        // --- 1. NAVBAR'I GÖRÜNÜR YAP ---
        // Kullanıcı bu sayfaya geldiğinde alt menünün kesinlikle göründüğünden emin olalım
        (activity as? MainActivity)?.setBottomNavVisibility(true)

        val rvBooks: RecyclerView = view.findViewById(R.id.rvBooks)
        val etSearch: TextInputEditText = view.findViewById(R.id.etSearch)
        val actvLevel: AutoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)

        rvBooks.layoutManager = GridLayoutManager(context, 2)

        // --- 2. TIKLAMA OLAYINI GÜNCELLEME ---
        bookAdapter = BookAdapter(displayList) { selectedBook ->
            // TIKLANINCA DETAY PENCERESİNİ AÇ
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

    // Fragment tekrar ekrana geldiğinde de Navbar'ı kontrol et
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisibility(true)
    }

    private fun fetchBooks() {
        db.collection("books").get().addOnSuccessListener { result ->
            originalList.clear()
            displayList.clear()
            for (document in result) {
                val book = document.toObject(Book::class.java)
                book.bookId = document.id // ID'yi kaydetmeyi unutma
                originalList.add(book)
            }
            displayList.addAll(originalList)
            bookAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(context, "Veriler alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
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
}