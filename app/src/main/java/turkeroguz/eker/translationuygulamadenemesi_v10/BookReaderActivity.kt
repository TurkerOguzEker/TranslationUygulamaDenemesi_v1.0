package turkeroguz.eker.translationuygulamadenemesi_v10

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Question

class BookReaderActivity : AppCompatActivity() {

    // Konteynerler
    private lateinit var layoutStoryContainer: LinearLayout
    private lateinit var layoutQuestionContainer: LinearLayout

    // Hikaye Elemanları
    private lateinit var tvChapterTitle: TextView
    private lateinit var tvChapterContent: TextView
    private lateinit var ivChapterImage: ImageView

    // Soru Elemanları
    private lateinit var tvQuestionText: TextView
    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button
    private lateinit var tvResult: TextView

    // İlerleme ve Kontrol Elemanları
    private lateinit var pbReadingProgress: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var btnNext: Button
    private lateinit var btnPrev: Button
    private lateinit var btnClose: ImageButton
    private lateinit var btnTextIncrease: Button
    private lateinit var btnTextDecrease: Button

    // Veri Değişkenleri
    private var currentBook: Book? = null
    private var currentIndex = 0
    private val flowList = ArrayList<Pair<String, Int>>()
    private val answeredQuestions = HashMap<Int, Int>()
    private var totalCorrect = 0
    private var isBookFinished = false
    private var highestProgress = 0

    // Yazı Boyutu Değişkeni
    private var currentTextSize = 18f

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_reader_split)

        currentBook = intent.getSerializableExtra("BOOK_DATA") as? Book
        if (currentBook == null) {
            Toast.makeText(this, "Hata: Kitap verisi yok!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        prepareFlow()
        checkPreviousProgress()
    }

    private fun initViews() {
        layoutStoryContainer = findViewById(R.id.layoutStoryContainer)
        layoutQuestionContainer = findViewById(R.id.layoutQuestionContainer)

        tvChapterTitle = findViewById(R.id.tvChapterTitle)
        tvChapterContent = findViewById(R.id.tvChapterContent)
        ivChapterImage = findViewById(R.id.ivChapterImage)

        tvQuestionText = findViewById(R.id.tvQuestionText)
        btnA = findViewById(R.id.btnOptA)
        btnB = findViewById(R.id.btnOptB)
        btnC = findViewById(R.id.btnOptC)
        btnD = findViewById(R.id.btnOptD)
        tvResult = findViewById(R.id.tvResult)

        pbReadingProgress = findViewById(R.id.pbReadingProgress)
        tvProgressText = findViewById(R.id.tvProgressText)

        btnNext = findViewById(R.id.btnReaderNext)
        btnPrev = findViewById(R.id.btnReaderPrev)
        btnClose = findViewById(R.id.btnCloseReader)
        btnTextIncrease = findViewById(R.id.btnTextIncrease)
        btnTextDecrease = findViewById(R.id.btnTextDecrease)

        // Yazı Boyutunu Başlangıç Değerine Ayarla
        tvChapterContent.textSize = currentTextSize

        // --- BUTON TIKLAMALARI ---
        btnTextIncrease.setOnClickListener {
            if (currentTextSize < 30f) { // Maksimum büyüklük sınırı
                currentTextSize += 2f
                tvChapterContent.textSize = currentTextSize
            }
        }

        btnTextDecrease.setOnClickListener {
            if (currentTextSize > 14f) { // Minimum küçüklük sınırı
                currentTextSize -= 2f
                tvChapterContent.textSize = currentTextSize
            }
        }

        btnNext.setOnClickListener { handleNextClick() }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadContent(currentIndex)
            }
        }

        btnClose.setOnClickListener { finish() }
    }

    private fun prepareFlow() {
        flowList.clear()
        val chapters = currentBook!!.chapters
        val questions = currentBook!!.questions
        val maxSize = maxOf(chapters.size, questions.size)

        for (i in 0 until maxSize) {
            if (i < chapters.size) flowList.add(Pair("CHAPTER", i))
            if (i < questions.size) flowList.add(Pair("QUESTION", i))
        }
    }

    private fun checkPreviousProgress() {
        val uid = auth.currentUser?.uid ?: return
        val bookId = currentBook!!.bookId

        db.collection("users").document(uid).collection("book_progress").document(bookId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    highestProgress = doc.getLong("progress")?.toInt() ?: 0
                    val savedIndex = doc.getLong("lastIndex")?.toInt() ?: 0

                    if (highestProgress == 100) isBookFinished = true

                    if (highestProgress == 100 && savedIndex >= flowList.size - 1) {
                        currentIndex = 0
                    } else {
                        currentIndex = if (savedIndex < flowList.size) savedIndex else 0
                    }
                }
                loadContent(currentIndex)
            }
            .addOnFailureListener {
                loadContent(currentIndex)
            }
    }

    private fun loadContent(index: Int) {
        if (index >= flowList.size) return

        btnPrev.isEnabled = index > 0
        btnPrev.alpha = if (index > 0) 1.0f else 0.5f

        val type = flowList[index].first
        val originalIndex = flowList[index].second

        if (type == "CHAPTER") {
            layoutStoryContainer.visibility = View.VISIBLE
            layoutQuestionContainer.visibility = View.GONE
            btnNext.text = "İLERİ (SORU)"

            val chapter = currentBook!!.chapters[originalIndex]
            tvChapterTitle.text = chapter.chapterTitle
            tvChapterContent.text = chapter.chapterText

            // Eğer resim URL'si varsa göster, yoksa gizle
            if (chapter.chapterImageUrl.isNotEmpty()) {
                ivChapterImage.visibility = View.VISIBLE
                Glide.with(this).load(chapter.chapterImageUrl).into(ivChapterImage)
            } else {
                ivChapterImage.visibility = View.GONE
            }

            calculateAndSaveProgress()

        } else {
            layoutStoryContainer.visibility = View.GONE
            layoutQuestionContainer.visibility = View.VISIBLE
            btnNext.text = "İLERİ (DEVAM)"

            val questionData = currentBook!!.questions[originalIndex]
            loadQuestionUI(questionData, originalIndex)

            calculateAndSaveProgress()
        }
    }

    private fun loadQuestionUI(q: Question, originalIndex: Int) {
        tvQuestionText.text = q.questionText
        tvResult.text = ""

        val buttons = listOf(btnA, btnB, btnC, btnD)
        val savedAnswerIndex = answeredQuestions[originalIndex]

        buttons.forEachIndexed { index, btn ->
            if (index < q.options.size) {
                btn.visibility = View.VISIBLE
                btn.text = q.options[index]
                btn.setTextColor(Color.BLACK)

                if (savedAnswerIndex != null) {
                    btn.isEnabled = false
                    if (index == q.correctOptionIndex) {
                        btn.setBackgroundColor(Color.parseColor("#4CAF50"))
                    } else if (index == savedAnswerIndex) {
                        btn.setBackgroundColor(Color.parseColor("#F44336"))
                    } else {
                        btn.setBackgroundColor(Color.LTGRAY)
                    }

                    if (savedAnswerIndex == q.correctOptionIndex) {
                        tvResult.text = "DOĞRU! 👏"
                        tvResult.setTextColor(Color.parseColor("#4CAF50"))
                    } else {
                        tvResult.text = "YANLIŞ 😔"
                        tvResult.setTextColor(Color.parseColor("#F44336"))
                    }
                } else {
                    btn.isEnabled = true
                    btn.setBackgroundColor(Color.LTGRAY)
                    btn.setOnClickListener {
                        checkAnswer(index, q.correctOptionIndex, originalIndex, buttons)
                    }
                }
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    private fun checkAnswer(selectedIndex: Int, correctIndex: Int, questionIndex: Int, buttons: List<Button>) {
        answeredQuestions[questionIndex] = selectedIndex
        buttons.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            buttons[selectedIndex].setBackgroundColor(Color.parseColor("#4CAF50"))
            tvResult.text = "DOĞRU! 👏"
            tvResult.setTextColor(Color.parseColor("#4CAF50"))
            totalCorrect++
        } else {
            buttons[selectedIndex].setBackgroundColor(Color.parseColor("#F44336"))
            buttons[correctIndex].setBackgroundColor(Color.parseColor("#4CAF50"))
            tvResult.text = "YANLIŞ 😔"
            tvResult.setTextColor(Color.parseColor("#F44336"))
        }
    }

    private fun handleNextClick() {
        val currentType = flowList[currentIndex].first
        val questionIndex = flowList[currentIndex].second

        if (currentType == "QUESTION" && !answeredQuestions.containsKey(questionIndex)) {
            Toast.makeText(this, "Lütfen devam etmeden önce soruyu cevaplayınız!", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentIndex < flowList.size - 1) {
            currentIndex++
            loadContent(currentIndex)
        } else {
            finishBook()

            // Eğer kitap bitmişse sonucu göster ve kapat
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Tebrikler! 🎉")
            builder.setMessage("Kitabı tamamladınız!\nDoğru Sayısı: $totalCorrect")
            builder.setPositiveButton("Bitir") { _, _ -> finish() }
            builder.setCancelable(false)
            builder.show()
        }
    }

    private fun calculateAndSaveProgress() {
        val totalParts = flowList.size
        val progressFloat = ((currentIndex + 1).toFloat() / totalParts.toFloat()) * 100
        var progressInt = progressFloat.toInt()

        if (progressInt > 100) progressInt = 100

        if (progressInt > highestProgress) {
            highestProgress = progressInt
        }

        pbReadingProgress.progress = highestProgress
        tvProgressText.text = "%$highestProgress"

        val uid = auth.currentUser?.uid ?: return
        val bookId = currentBook!!.bookId

        val progressData = mapOf(
            "progress" to highestProgress,
            "lastIndex" to currentIndex
        )

        db.collection("users").document(uid).collection("book_progress").document(bookId)
            .set(progressData)

        if (highestProgress == 100 && !isBookFinished) {
            finishBook()
        }
    }

    private fun finishBook() {
        if (isBookFinished) return
        isBookFinished = true

        pbReadingProgress.progress = 100
        tvProgressText.text = "%100"

        val uid = auth.currentUser?.uid ?: return
        val bookId = currentBook!!.bookId

        db.collection("users").document(uid).collection("finished_books").document(bookId)
            .set(currentBook!!)
            .addOnSuccessListener {
                Toast.makeText(this, "Kitap bitirilenlere eklendi.", Toast.LENGTH_SHORT).show()
            }
    }
}