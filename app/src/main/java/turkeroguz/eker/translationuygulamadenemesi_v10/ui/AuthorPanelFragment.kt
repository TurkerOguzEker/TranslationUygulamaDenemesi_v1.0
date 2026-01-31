package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import java.util.Locale

class AuthorPanelFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    // Arama ve filtreleme için çift liste kullanıyoruz
    private val originalList = ArrayList<Book>()
    private val displayList = ArrayList<Book>()

    private lateinit var adapter: BookAdapter
    private val levels = arrayOf("Seçiniz", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
    private val filterLevels = arrayOf("Tümü", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_author_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvAuthorBooks)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddBook)
        val etSearch = view.findViewById<EditText>(R.id.etSearchAuthor)
        val spFilter = view.findViewById<Spinner>(R.id.spFilterLevel)

        // Listeyi Başlat
        rvBooks.layoutManager = GridLayoutManager(context, 2)
        adapter = BookAdapter(displayList) { selectedBook ->
            showAddEditDialog(selectedBook)
        }
        rvBooks.adapter = adapter

        // Ana Ekran Filtre Spinner'ını Ayarla
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterLevels)
        spFilter.adapter = filterAdapter

        // Verileri Çek
        loadBooks()

        // --- ARAMA KUTUSU DİNLEYİCİSİ ---
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val selectedLevel = spFilter.selectedItem.toString()
                filterList(query, selectedLevel)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- FİLTRE SPINNER DİNLEYİCİSİ ---
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLevel = filterLevels[position]
                val query = etSearch.text.toString()
                filterList(query, selectedLevel)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Yeni Ekle Butonu
        fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun loadBooks() {
        db.collection("books").get().addOnSuccessListener { result ->
            originalList.clear()
            displayList.clear()
            for (document in result) {
                val book = document.toObject(Book::class.java)
                book.bookId = document.id // ID'yi mutlaka al
                originalList.add(book)
            }
            displayList.addAll(originalList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun filterList(query: String, level: String) {
        displayList.clear()
        val filtered = originalList.filter { book ->
            val matchQuery = book.title.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                    book.bookId.contains(query) // ID ile de arama yapabilir

            val matchLevel = (level == "Tümü" || book.level == level)

            matchQuery && matchLevel
        }
        displayList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun showAddEditDialog(bookToEdit: Book?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_book, null)
        val builder = AlertDialog.Builder(context).setView(dialogView)
        val dialog = builder.create()

        // Görünümleri Bağla
        val tvIdInfo = dialogView.findViewById<TextView>(R.id.tvBookIdInfo)
        val etTitle = dialogView.findViewById<EditText>(R.id.etBookTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etBookAuthor)
        val spLevel = dialogView.findViewById<Spinner>(R.id.spBookLevel) // Spinner Bağlantısı
        val etImage = dialogView.findViewById<EditText>(R.id.etBookImage)
        val etPdf = dialogView.findViewById<EditText>(R.id.etBookPdf)
        val etDesc = dialogView.findViewById<EditText>(R.id.etBookDesc)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBook)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDeleteBook)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        // Spinner (Seviye Seçimi) için Adaptör
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, levels)
        spLevel.adapter = levelAdapter

        // DÜZENLEME MODU MU?
        if (bookToEdit != null) {
            tvTitle.text = "Kitabı Düzenle"
            tvIdInfo.text = "ID: ${bookToEdit.bookId}" // ID'yi göster

            etTitle.setText(bookToEdit.title)
            etAuthor.setText(bookToEdit.author)
            etImage.setText(bookToEdit.imageUrl)
            etPdf.setText(bookToEdit.pdfUrl)
            etDesc.setText(bookToEdit.description)

            // Mevcut seviyeyi Spinner'da seçili hale getir
            val spinnerPosition = levelAdapter.getPosition(bookToEdit.level)
            if (spinnerPosition >= 0) {
                spLevel.setSelection(spinnerPosition)
            }

            btnDelete.visibility = View.VISIBLE
        } else {
            tvTitle.text = "Yeni Kitap Ekle"
            tvIdInfo.text = "ID: (Otomatik Oluşturulacak)"
            btnDelete.visibility = View.GONE
        }

        // KAYDET BUTONU
        btnSave.setOnClickListener {
            // Spinner'dan seçilen seviyeyi al
            val selectedLevel = spLevel.selectedItem.toString()
            if (selectedLevel == "Seçiniz") {
                Toast.makeText(context, "Lütfen bir seviye seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Boş veri haritası
            val bookData = hashMapOf(
                "title" to etTitle.text.toString(),
                "author" to etAuthor.text.toString(),
                "level" to selectedLevel, // Spinner'dan gelen veri
                "imageUrl" to etImage.text.toString(),
                "pdfUrl" to etPdf.text.toString(),
                "description" to etDesc.text.toString()
            )

            btnSave.isEnabled = false // Çift tıklamayı önle

            if (bookToEdit == null) {
                // --- YENİ KAYIT ---
                db.collection("books").add(bookData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Kitap Eklendi ✅", Toast.LENGTH_SHORT).show()
                        loadBooks() // Listeyi yenile
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                        btnSave.isEnabled = true
                    }
            } else {
                // --- GÜNCELLEME ---
                // Düzeltme: bookToEdit.bookId'nin doğru geldiğinden emin oluyoruz
                db.collection("books").document(bookToEdit.bookId).update(bookData as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Kitap Güncellendi 🔄", Toast.LENGTH_SHORT).show()
                        loadBooks()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Güncelleme Hatası: ${it.message}", Toast.LENGTH_SHORT).show()
                        btnSave.isEnabled = true
                    }
            }
        }

        // SİL BUTONU
        btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Kitabı Sil")
                .setMessage("Bu işlem geri alınamaz. Emin misiniz?")
                .setPositiveButton("Sil") { _, _ ->
                    if (bookToEdit != null) {
                        db.collection("books").document(bookToEdit.bookId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Kitap Silindi 🗑️", Toast.LENGTH_SHORT).show()
                                loadBooks()
                                dialog.dismiss()
                            }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        dialog.show()
    }
}