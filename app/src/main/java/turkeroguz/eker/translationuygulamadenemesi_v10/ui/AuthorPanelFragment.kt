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
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Question
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

        // --- GÖRÜNÜMLERİ BAĞLA ---
        val tvIdInfo = dialogView.findViewById<TextView>(R.id.tvBookIdInfo)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        // Temel Bilgiler
        val etTitle = dialogView.findViewById<EditText>(R.id.etBookTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etBookAuthor)
        val spLevel = dialogView.findViewById<Spinner>(R.id.spBookLevel)
        val etImage = dialogView.findViewById<EditText>(R.id.etBookImage)
        val etDesc = dialogView.findViewById<EditText>(R.id.etBookDesc)

        // Bölüm 1 - Hikaye ve Soru Alanları (LİNK ALANLARI)
        val etStory1 = dialogView.findViewById<EditText>(R.id.etStory1)

        // DEĞİŞİKLİK: Metin alanı (etQuestionText1) yerine PDF Link alanı (etQuestionPdf1)
        val etQPdf1 = dialogView.findViewById<EditText>(R.id.etQuestionPdf1)

        val etOpt1A = dialogView.findViewById<EditText>(R.id.etOpt1A)
        val etOpt1B = dialogView.findViewById<EditText>(R.id.etOpt1B)
        val etOpt1C = dialogView.findViewById<EditText>(R.id.etOpt1C)
        val etOpt1D = dialogView.findViewById<EditText>(R.id.etOpt1D)
        val spCorrect1 = dialogView.findViewById<Spinner>(R.id.spCorrect1)

        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBook)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDeleteBook)

        // Seviye Spinner Ayarı
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, levels)
        spLevel.adapter = levelAdapter

        // --- DÜZENLEME MODU KONTROLÜ ---
        if (bookToEdit != null) {
            tvTitle.text = "Kitabı Düzenle"
            tvIdInfo.text = "ID: ${bookToEdit.bookId}"

            etTitle.setText(bookToEdit.title)
            etAuthor.setText(bookToEdit.author)
            etImage.setText(bookToEdit.imageUrl)
            etDesc.setText(bookToEdit.description)

            // Seviyeyi seç
            val spinnerPosition = levelAdapter.getPosition(bookToEdit.level)
            if (spinnerPosition >= 0) spLevel.setSelection(spinnerPosition)

            // 1. Bölüm Verilerini Doldur (Eğer varsa)
            if (bookToEdit.storyUrls.isNotEmpty()) {
                etStory1.setText(bookToEdit.storyUrls[0])
            }

            if (bookToEdit.questions.isNotEmpty()) {
                val q1 = bookToEdit.questions[0]
                // DEĞİŞİKLİK: questionPdfUrl alanını doldur
                etQPdf1.setText(q1.questionPdfUrl)

                if (q1.options.size >= 4) {
                    etOpt1A.setText(q1.options[0])
                    etOpt1B.setText(q1.options[1])
                    etOpt1C.setText(q1.options[2])
                    etOpt1D.setText(q1.options[3])
                }
                spCorrect1.setSelection(q1.correctOptionIndex)
            }

            btnDelete.visibility = View.VISIBLE
        } else {
            tvTitle.text = "Yeni Kitap Ekle"
            tvIdInfo.text = "ID: (Otomatik Oluşturulacak)"
            btnDelete.visibility = View.GONE
        }

        // --- KAYDET BUTONU ---
        btnSave.setOnClickListener {
            // Seviye Kontrolü
            val selectedLevel = spLevel.selectedItem.toString()
            if (selectedLevel == "Seçiniz") {
                Toast.makeText(context, "Lütfen bir seviye seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- VERİLERİ HAZIRLA ---

            // 1. Hikaye Listesi
            val storiesList = ArrayList<String>()
            if (etStory1.text.isNotEmpty()) {
                storiesList.add(etStory1.text.toString())
            }

            // 2. Soru Listesi (Question Nesneleri)
            val questionsList = ArrayList<Question>()

            // 1. Soruyu Kontrol Et ve Ekle (PDF LİNKİ OLARAK)
            if (etQPdf1.text.isNotEmpty()) { // DEĞİŞİKLİK: etQPdf1 kontrolü
                val q1 = Question(
                    questionPdfUrl = etQPdf1.text.toString(), // DEĞİŞİKLİK: PdfUrl olarak kaydet
                    options = listOf(
                        etOpt1A.text.toString(),
                        etOpt1B.text.toString(),
                        etOpt1C.text.toString(),
                        etOpt1D.text.toString()
                    ),
                    correctOptionIndex = spCorrect1.selectedItemPosition
                )
                questionsList.add(q1)
            }

            // --- FIREBASE VERİSİ ---
            val bookData = hashMapOf(
                "title" to etTitle.text.toString(),
                "author" to etAuthor.text.toString(),
                "level" to selectedLevel,
                "imageUrl" to etImage.text.toString(),
                "description" to etDesc.text.toString(),

                "storyUrls" to storiesList,
                "questions" to questionsList
            )

            btnSave.isEnabled = false // Çift tıklamayı önle

            if (bookToEdit == null) {
                // YENİ KAYIT
                db.collection("books").add(bookData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Kitap ve Bölümler Eklendi ✅", Toast.LENGTH_SHORT).show()
                        loadBooks()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                        btnSave.isEnabled = true
                    }
            } else {
                // GÜNCELLEME
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

        // --- SİL BUTONU ---
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