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
    private val originalList = ArrayList<Book>()
    private val displayList = ArrayList<Book>()
    private lateinit var adapter: BookAdapter
    private val levels = arrayOf("Seçiniz", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
    private val filterLevels = arrayOf("Tümü", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
    private val optionLetters = arrayOf("A", "B", "C", "D") // Spinner için

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_author_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvAuthorBooks)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddBook)
        val etSearch = view.findViewById<EditText>(R.id.etSearchAuthor)
        val spFilter = view.findViewById<Spinner>(R.id.spFilterLevel)

        rvBooks.layoutManager = GridLayoutManager(context, 2)
        adapter = BookAdapter(displayList) { selectedBook -> showAddEditDialog(selectedBook) }
        rvBooks.adapter = adapter

        spFilter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterLevels)

        loadBooks()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList(s.toString(), spFilter.selectedItem.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterList(etSearch.text.toString(), filterLevels[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        fabAdd.setOnClickListener { showAddEditDialog(null) }
    }

    private fun loadBooks() {
        db.collection("books").get().addOnSuccessListener { result ->
            originalList.clear()
            displayList.clear()
            for (document in result) {
                val book = document.toObject(Book::class.java)
                book.bookId = document.id
                originalList.add(book)
            }
            displayList.addAll(originalList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun filterList(query: String, level: String) {
        displayList.clear()
        val filtered = originalList.filter { book ->
            val matchQuery = book.title.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
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

        val etTitle = dialogView.findViewById<EditText>(R.id.etBookTitle)
        val etAuthor = dialogView.findViewById<EditText>(R.id.etBookAuthor)
        val spLevel = dialogView.findViewById<Spinner>(R.id.spBookLevel)
        val etImage = dialogView.findViewById<EditText>(R.id.etBookImage)
        val etDesc = dialogView.findViewById<EditText>(R.id.etBookDesc)
        val llChapters = dialogView.findViewById<LinearLayout>(R.id.llChaptersContainer)
        val btnAddChapter = dialogView.findViewById<Button>(R.id.btnAddChapter)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBook)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDeleteBook)
        val tvIdInfo = dialogView.findViewById<TextView>(R.id.tvBookIdInfo)

        spLevel.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, levels)

        // Bölüm Ekleme Fonksiyonu
        fun addChapterView(storyUrl: String = "", question: Question? = null) {
            val chapterView = LayoutInflater.from(context).inflate(R.layout.layout_chapter_item, null)
            val index = llChapters.childCount + 1

            chapterView.findViewById<TextView>(R.id.tvChapterTitle).text = "$index. Bölüm"

            val etStory = chapterView.findViewById<EditText>(R.id.etChapterStoryPdf)
            val etQuestion = chapterView.findViewById<EditText>(R.id.etChapterQuestionPdf)
            val etA = chapterView.findViewById<EditText>(R.id.etOptA)
            val etB = chapterView.findViewById<EditText>(R.id.etOptB)
            val etC = chapterView.findViewById<EditText>(R.id.etOptC)
            val etD = chapterView.findViewById<EditText>(R.id.etOptD)
            val spCorrect = chapterView.findViewById<Spinner>(R.id.spChapterCorrect)
            val btnRemove = chapterView.findViewById<Button>(R.id.btnRemoveChapter)

            spCorrect.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, optionLetters)

            // Var olan veriyi doldur
            etStory.setText(storyUrl)
            if (question != null) {
                etQuestion.setText(question.questionPdfUrl)
                if (question.options.size >= 4) {
                    etA.setText(question.options[0])
                    etB.setText(question.options[1])
                    etC.setText(question.options[2])
                    etD.setText(question.options[3])
                }
                spCorrect.setSelection(question.correctOptionIndex)
            }

            btnRemove.setOnClickListener {
                llChapters.removeView(chapterView)
            }

            llChapters.addView(chapterView)
        }

        // --- VERİLERİ YÜKLE ---
        if (bookToEdit != null) {
            tvIdInfo.text = "ID: ${bookToEdit.bookId}"
            etTitle.setText(bookToEdit.title)
            etAuthor.setText(bookToEdit.author)
            etImage.setText(bookToEdit.imageUrl)
            etDesc.setText(bookToEdit.description)
            val pos = (spLevel.adapter as ArrayAdapter<String>).getPosition(bookToEdit.level)
            spLevel.setSelection(pos)
            btnDelete.visibility = View.VISIBLE

            // Var olan bölümleri yükle
            val maxLen = maxOf(bookToEdit.storyUrls.size, bookToEdit.questions.size)
            for (i in 0 until maxLen) {
                val sUrl = if (i < bookToEdit.storyUrls.size) bookToEdit.storyUrls[i] else ""
                val qData = if (i < bookToEdit.questions.size) bookToEdit.questions[i] else null
                addChapterView(sUrl, qData)
            }
        } else {
            // Yeni kitap ise 1 tane boş bölüm ekle
            addChapterView()
        }

        // --- BUTON İŞLEMLERİ ---
        btnAddChapter.setOnClickListener { addChapterView() }

        btnSave.setOnClickListener {
            val selectedLevel = spLevel.selectedItem.toString()
            if (selectedLevel == "Seçiniz") {
                Toast.makeText(context, "Seviye seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Bölümleri Topla
            val storyList = ArrayList<String>()
            val questionList = ArrayList<Question>()

            for (i in 0 until llChapters.childCount) {
                val view = llChapters.getChildAt(i)
                val etS = view.findViewById<EditText>(R.id.etChapterStoryPdf)
                val etQ = view.findViewById<EditText>(R.id.etChapterQuestionPdf)
                val etA = view.findViewById<EditText>(R.id.etOptA)
                val etB = view.findViewById<EditText>(R.id.etOptB)
                val etC = view.findViewById<EditText>(R.id.etOptC)
                val etD = view.findViewById<EditText>(R.id.etOptD)
                val spC = view.findViewById<Spinner>(R.id.spChapterCorrect)

                if (etS.text.isNotEmpty()) storyList.add(etS.text.toString())

                if (etQ.text.isNotEmpty()) {
                    val q = Question(
                        questionPdfUrl = etQ.text.toString(),
                        options = listOf(etA.text.toString(), etB.text.toString(), etC.text.toString(), etD.text.toString()),
                        correctOptionIndex = spC.selectedItemPosition
                    )
                    questionList.add(q)
                }
            }

            val bookData = hashMapOf(
                "title" to etTitle.text.toString(),
                "author" to etAuthor.text.toString(),
                "level" to selectedLevel,
                "imageUrl" to etImage.text.toString(),
                "description" to etDesc.text.toString(),
                "storyUrls" to storyList,
                "questions" to questionList
            )

            btnSave.isEnabled = false

            if (bookToEdit == null) {
                db.collection("books").add(bookData).addOnSuccessListener {
                    Toast.makeText(context, "Kitap Kaydedildi! ✅", Toast.LENGTH_SHORT).show()
                    loadBooks()
                    dialog.dismiss()
                }
            } else {
                // HATA DÜZELTİLDİ: 'as Map<String, Any>' eklendi
                db.collection("books").document(bookToEdit.bookId).update(bookData as Map<String, Any>).addOnSuccessListener {
                    Toast.makeText(context, "Kitap Güncellendi! 🔄", Toast.LENGTH_SHORT).show()
                    loadBooks()
                    dialog.dismiss()
                }
            }
        }

        btnDelete.setOnClickListener {
            if (bookToEdit != null) {
                db.collection("books").document(bookToEdit.bookId).delete().addOnSuccessListener {
                    loadBooks(); dialog.dismiss()
                }
            }
        }
        dialog.show()
    }
}