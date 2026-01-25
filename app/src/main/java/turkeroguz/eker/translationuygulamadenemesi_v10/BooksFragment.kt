package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class BooksFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    // Veritabanından gelen tüm kitapları burada tutacağız
    private val allBooks = ArrayList<Book>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        // 1. RecyclerView Ayarları
        val rvBooks: RecyclerView = view.findViewById(R.id.rvBooks)
        // Yan yana 2 kitap olacak şekilde Grid Layout
        rvBooks.layoutManager = GridLayoutManager(context, 2)

        // 2. Dropdown (Seviye Seçimi) Ayarları
        // Eğer strings.xml içinde levels_array yoksa, çökmemesi için manuel liste ekledim:
        val levels = try {
            resources.getStringArray(R.array.levels_array)
        } catch (e: Exception) {
            // Yedek liste
            arrayOf("Tümü", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        val autoCompleteTextView: AutoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)

        // 3. Kitapları Veritabanından Çek
        fetchBooks(rvBooks)

        // 4. Seviye Seçilince Listeyi Filtrele
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedLevel = parent.getItemAtPosition(position).toString()
            filterBooks(selectedLevel, rvBooks)
        }

        return view
    }

    private fun fetchBooks(recyclerView: RecyclerView) {
        db.collection("books")
            .get()
            .addOnSuccessListener { result ->
                allBooks.clear()
                for (document in result) {
                    val book = document.toObject(Book::class.java)
                    // Firestore ID'sini modele ekle
                    book.bookId = document.id
                    allBooks.add(book)
                }
                // İlk açılışta tüm kitapları göster
                updateAdapter(allBooks, recyclerView)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterBooks(level: String, recyclerView: RecyclerView) {
        // "Tümü" veya "All" seçilirse hepsini göster, yoksa filtrele
        if (level.equals("Tümü", ignoreCase = true) || level.equals("All", ignoreCase = true)) {
            updateAdapter(allBooks, recyclerView)
        } else {
            val filteredList = allBooks.filter { it.level == level }
            updateAdapter(filteredList, recyclerView)
        }
    }

    private fun updateAdapter(books: List<Book>, recyclerView: RecyclerView) {
        // Yeni BookAdapter ile listeyi güncelle
        val adapter = BookAdapter(books) { selectedBook ->
            // Kitaba tıklanma olayı
            Toast.makeText(context, "${selectedBook.title} seçildi", Toast.LENGTH_SHORT).show()

            // İleride detay sayfasına gitmek için burayı kullanacağız:
            /*
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("bookId", selectedBook.bookId)
            startActivity(intent)
            */
        }
        recyclerView.adapter = adapter
    }
}