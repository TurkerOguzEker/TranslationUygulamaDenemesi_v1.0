package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.WordAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Word

class WordsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val allWords = mutableListOf<Word>()
    private val displayWords = mutableListOf<Word>()
    private lateinit var wordAdapter: WordAdapter
    private var wordsListener: ListenerRegistration? = null

    private var quizList = mutableListOf<Word>()
    private var quizIndex = 0
    private var isAnswerRevealed = false

    private lateinit var layoutListTab: View
    private lateinit var layoutQuizTab: View
    private lateinit var btnTabList: MaterialButton
    private lateinit var btnTabQuiz: MaterialButton
    private lateinit var fabAddWord: FloatingActionButton
    private lateinit var rvWords: RecyclerView
    private lateinit var tvEmptyWords: TextView
    private lateinit var tvWordCount: TextView
    private lateinit var tvLearnedCount: TextView
    private lateinit var etSearch: TextInputEditText

    private lateinit var tvQuizProgress: TextView
    private lateinit var cardFlashcard: MaterialCardView
    private lateinit var tvFlashcardEnglish: TextView
    private lateinit var tvFlashcardTurkish: TextView
    private lateinit var tvTapToReveal: TextView
    private lateinit var btnQuizSkip: MaterialButton
    private lateinit var btnQuizLearned: MaterialButton
    private lateinit var tvEmptyQuiz: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_words, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisibility(true)

        initViews(view)
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupQuiz()
        setupFab()
        loadWords()
    }

    private fun initViews(view: View) {
        layoutListTab = view.findViewById(R.id.layoutListTab)
        layoutQuizTab = view.findViewById(R.id.layoutQuizTab)
        btnTabList = view.findViewById(R.id.btnTabList)
        btnTabQuiz = view.findViewById(R.id.btnTabQuiz)
        fabAddWord = view.findViewById(R.id.fabAddWord)
        rvWords = view.findViewById(R.id.rvWords)
        tvEmptyWords = view.findViewById(R.id.tvEmptyWords)
        tvWordCount = view.findViewById(R.id.tvWordCount)
        tvLearnedCount = view.findViewById(R.id.tvLearnedCount)
        etSearch = view.findViewById(R.id.etSearchWord)

        tvQuizProgress = view.findViewById(R.id.tvQuizProgress)
        cardFlashcard = view.findViewById(R.id.cardFlashcard)
        tvFlashcardEnglish = view.findViewById(R.id.tvFlashcardEnglish)
        tvFlashcardTurkish = view.findViewById(R.id.tvFlashcardTurkish)
        tvTapToReveal = view.findViewById(R.id.tvTapToReveal)
        btnQuizSkip = view.findViewById(R.id.btnQuizSkip)
        btnQuizLearned = view.findViewById(R.id.btnQuizLearned)
        tvEmptyQuiz = view.findViewById(R.id.tvEmptyQuiz)
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(
            displayWords,
            onToggleLearned = { word -> toggleLearned(word) },
            onDelete = { word -> deleteWord(word) }
        )
        rvWords.layoutManager = LinearLayoutManager(requireContext())
        rvWords.adapter = wordAdapter
    }

    private fun setupTabs() {
        btnTabList.setOnClickListener { showListTab() }
        btnTabQuiz.setOnClickListener { showQuizTab() }
        showListTab()
    }

    private fun showListTab() {
        layoutListTab.visibility = View.VISIBLE
        layoutQuizTab.visibility = View.GONE
        fabAddWord.show()
        btnTabList.alpha = 1.0f
        btnTabQuiz.alpha = 0.5f
    }

    private fun showQuizTab() {
        layoutListTab.visibility = View.GONE
        layoutQuizTab.visibility = View.VISIBLE
        fabAddWord.hide()
        btnTabList.alpha = 0.5f
        btnTabQuiz.alpha = 1.0f
        startQuiz()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterWords(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFab() {
        fabAddWord.setOnClickListener { showAddWordDialog() }
    }

    private fun setupQuiz() {
        cardFlashcard.setOnClickListener {
            if (!isAnswerRevealed && quizList.isNotEmpty()) {
                tvFlashcardTurkish.visibility = View.VISIBLE
                tvTapToReveal.visibility = View.GONE
                isAnswerRevealed = true
            }
        }

        btnQuizSkip.setOnClickListener {
            quizIndex++
            loadQuizCard()
        }

        btnQuizLearned.setOnClickListener {
            if (quizList.isNotEmpty() && quizIndex < quizList.size) {
                val word = quizList[quizIndex]
                markAsLearned(word)
                quizList.removeAt(quizIndex)
                if (quizIndex >= quizList.size) quizIndex = 0
                loadQuizCard()
            }
        }
    }

    private fun loadWords() {
        val uid = auth.currentUser?.uid ?: return

        wordsListener = db.collection("users").document(uid).collection("words")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                allWords.clear()
                for (doc in snapshot.documents) {
                    val word = doc.toObject(Word::class.java) ?: continue
                    word.wordId = doc.id
                    allWords.add(word)
                }

                filterWords(etSearch.text.toString())
                updateStats()
            }
    }

    private fun filterWords(query: String) {
        displayWords.clear()
        val filtered = if (query.isEmpty()) {
            allWords
        } else {
            allWords.filter {
                it.english.contains(query, ignoreCase = true) ||
                it.turkish.contains(query, ignoreCase = true)
            }
        }
        displayWords.addAll(filtered)
        wordAdapter.notifyDataSetChanged()

        if (displayWords.isEmpty()) {
            rvWords.visibility = View.GONE
            tvEmptyWords.visibility = View.VISIBLE
        } else {
            rvWords.visibility = View.VISIBLE
            tvEmptyWords.visibility = View.GONE
        }
    }

    private fun updateStats() {
        val total = allWords.size
        val learned = allWords.count { it.isLearned }
        tvWordCount.text = "Toplam: $total"
        tvLearnedCount.text = "Öğrenilen: $learned"
    }

    private fun startQuiz() {
        quizList = allWords.filter { !it.isLearned }.shuffled().toMutableList()
        quizIndex = 0
        loadQuizCard()
    }

    private fun loadQuizCard() {
        if (quizList.isEmpty()) {
            cardFlashcard.visibility = View.GONE
            btnQuizSkip.visibility = View.GONE
            btnQuizLearned.visibility = View.GONE
            tvQuizProgress.visibility = View.GONE
            tvEmptyQuiz.visibility = View.VISIBLE
            return
        }

        if (quizIndex >= quizList.size) quizIndex = 0

        cardFlashcard.visibility = View.VISIBLE
        btnQuizSkip.visibility = View.VISIBLE
        btnQuizLearned.visibility = View.VISIBLE
        tvQuizProgress.visibility = View.VISIBLE
        tvEmptyQuiz.visibility = View.GONE

        isAnswerRevealed = false
        val word = quizList[quizIndex]
        tvFlashcardEnglish.text = word.english
        tvFlashcardTurkish.text = word.turkish
        tvFlashcardTurkish.visibility = View.INVISIBLE
        tvTapToReveal.visibility = View.VISIBLE
        tvQuizProgress.text = "${quizIndex + 1} / ${quizList.size}"
    }

    private fun showAddWordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_word, null)
        val etEnglish = dialogView.findViewById<TextInputEditText>(R.id.etEnglishWord)
        val etTurkish = dialogView.findViewById<TextInputEditText>(R.id.etTurkishWord)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Kelime Ekle")
            .setView(dialogView)
            .setPositiveButton("Ekle") { _, _ ->
                val english = etEnglish.text.toString().trim()
                val turkish = etTurkish.text.toString().trim()
                if (english.isEmpty() || turkish.isEmpty()) {
                    Toast.makeText(requireContext(), "Lütfen her iki alanı da doldurun.", Toast.LENGTH_SHORT).show()
                } else {
                    saveWord(english, turkish)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun saveWord(english: String, turkish: String) {
        val uid = auth.currentUser?.uid ?: return
        val word = hashMapOf(
            "english" to english,
            "turkish" to turkish,
            "addedAt" to System.currentTimeMillis(),
            "isLearned" to false
        )
        db.collection("users").document(uid).collection("words")
            .add(word)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "\"$english\" eklendi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Eklenemedi.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleLearned(word: Word) {
        val uid = auth.currentUser?.uid ?: return
        val newStatus = !word.isLearned
        db.collection("users").document(uid).collection("words").document(word.wordId)
            .update("isLearned", newStatus)
    }

    private fun markAsLearned(word: Word) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("words").document(word.wordId)
            .update("isLearned", true)
    }

    private fun deleteWord(word: Word) {
        val uid = auth.currentUser?.uid ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Kelimeyi Sil")
            .setMessage("\"${word.english}\" silinsin mi?")
            .setPositiveButton("Sil") { _, _ ->
                db.collection("users").document(uid).collection("words").document(word.wordId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Silindi.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisibility(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        wordsListener?.remove()
    }
}
