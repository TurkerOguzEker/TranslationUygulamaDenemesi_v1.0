package turkeroguz.eker.translationuygulamadenemesi_v10.ui

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder // YENİ EKLENDİ (Modern Dialog)
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Chapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Question
import java.util.Locale

class AuthorPanelFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val originalList = ArrayList<Book>()
    private val displayList = ArrayList<Book>()
    private lateinit var adapter: BookAdapter
    private val levels = arrayOf("Seçiniz", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
    private val filterLevels = arrayOf("Tümü", "A1", "A2", "B1", "B1+", "B2", "C1", "C2")
    private val optionLetters = arrayOf("A", "B", "C", "D")

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
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(dialogView)
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

        fun addChapterView(chapter: Chapter? = null, question: Question? = null) {
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

            if (chapter != null) { etStory.setText(chapter.chapterText) }
            if (question != null) {
                etQuestion.setText(question.questionText)
                if (question.options.size >= 4) {
                    etA.setText(question.options[0])
                    etB.setText(question.options[1])
                    etC.setText(question.options[2])
                    etD.setText(question.options[3])
                }
                spCorrect.setSelection(question.correctOptionIndex)
            }

            btnRemove.setOnClickListener { llChapters.removeView(chapterView) }
            llChapters.addView(chapterView)
        }

        if (bookToEdit != null) {
            tvIdInfo.text = "ID: ${bookToEdit.bookId}"
            etTitle.setText(bookToEdit.title)
            etAuthor.setText(bookToEdit.author)
            etImage.setText(bookToEdit.imageUrl)
            etDesc.setText(bookToEdit.description)
            val pos = (spLevel.adapter as ArrayAdapter<String>).getPosition(bookToEdit.level)
            spLevel.setSelection(pos)
            btnDelete.visibility = View.VISIBLE

            val maxLen = maxOf(bookToEdit.chapters.size, bookToEdit.questions.size)
            for (i in 0 until maxLen) {
                val chapterData = if (i < bookToEdit.chapters.size) bookToEdit.chapters[i] else null
                val qData = if (i < bookToEdit.questions.size) bookToEdit.questions[i] else null
                addChapterView(chapterData, qData)
            }
        } else {
            addChapterView()
        }

        btnAddChapter.setOnClickListener { addChapterView() }

        // --- KAYDETME / GÜNCELLEME İÇİN MODERN ONAY PENCERESİ ---
        btnSave.setOnClickListener {
            val selectedLevel = spLevel.selectedItem.toString()
            if (selectedLevel == "Seçiniz") {
                Toast.makeText(context, "Lütfen bir seviye seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isNewBook = (bookToEdit == null)
            val titleStr = if (isNewBook) "Yeni Kitap Ekle" else "Kitabı Güncelle"
            val messageStr = if (isNewBook) "Bu kitabı veritabanına eklemek istediğinize emin misiniz?"
            else "Kitaptaki değişiklikleri kaydetmek istediğinize emin misiniz?"

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleStr)
                .setMessage(messageStr)
                .setIcon(R.drawable.ic_check) // Varsa uygun bir ikon koyabilirsin
                .setPositiveButton("Evet, Kaydet") { _, _ ->

                    val chapterList = ArrayList<Chapter>()
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

                        val storyText = etS.text.toString()
                        if (storyText.isNotEmpty()) {
                            chapterList.add(Chapter("${i + 1}. Bölüm", storyText, ""))
                        }

                        val questionText = etQ.text.toString()
                        if (questionText.isNotEmpty()) {
                            questionList.add(Question(
                                questionText,
                                listOf(etA.text.toString(), etB.text.toString(), etC.text.toString(), etD.text.toString()),
                                spC.selectedItemPosition
                            ))
                        }
                    }

                    val bookData = hashMapOf(
                        "title" to etTitle.text.toString(),
                        "author" to etAuthor.text.toString(),
                        "level" to selectedLevel,
                        "imageUrl" to etImage.text.toString(),
                        "description" to etDesc.text.toString(),
                        "chapters" to chapterList,
                        "questions" to questionList
                    )

                    btnSave.isEnabled = false

                    if (isNewBook) {
                        db.collection("books").add(bookData).addOnSuccessListener {
                            Toast.makeText(context, "Kitap Başarıyla Eklendi! ✅", Toast.LENGTH_SHORT).show()
                            loadBooks()
                            dialog.dismiss()
                        }
                    } else {
                        db.collection("books").document(bookToEdit!!.bookId).update(bookData as Map<String, Any>).addOnSuccessListener {
                            Toast.makeText(context, "Kitap Güncellendi! 🔄", Toast.LENGTH_SHORT).show()
                            loadBooks()
                            dialog.dismiss()
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // --- SİLME İÇİN MODERN ONAY PENCERESİ ---
        btnDelete.setOnClickListener {
            if (bookToEdit != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Kitabı Tamamen Sil")
                    .setMessage("Bu kitabı kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz ve kullanıcıların bu kitaba erişimi tamamen kesilir.")
                    .setIcon(R.drawable.ic_delete) // Çöp kutusu ikonu
                    .setPositiveButton("Evet, Sil") { _, _ ->
                        db.collection("books").document(bookToEdit.bookId).delete().addOnSuccessListener {
                            Toast.makeText(context, "Kitap başarıyla silindi.", Toast.LENGTH_SHORT).show()
                            loadBooks()
                            dialog.dismiss()
                        }
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        }
        dialog.show()
    }
}