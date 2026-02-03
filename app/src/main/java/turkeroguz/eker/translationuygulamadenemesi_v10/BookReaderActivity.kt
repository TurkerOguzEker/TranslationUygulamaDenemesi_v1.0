package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Question
import java.net.URL

class BookReaderActivity : AppCompatActivity() {

    private lateinit var layoutStory: LinearLayout
    private lateinit var layoutQuestion: LinearLayout

    private lateinit var pdfViewStory: PDFView
    private lateinit var pdfViewQuestion: PDFView
    private lateinit var progressBarStory: ProgressBar

    private lateinit var tvInfo: TextView
    private lateinit var btnNext: Button
    private lateinit var btnPrev: Button
    private lateinit var btnClose: ImageButton
    private lateinit var tvResult: TextView

    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button

    private var currentBook: Book? = null

    // Akış Listesi
    private val flowList = ArrayList<Pair<String, Int>>()
    private var currentIndex = 0
    private var totalCorrect = 0

    // Çözülen Soruları Tutmak İçin (Soru Index -> Verilen Cevap Index)
    private val answeredQuestions = HashMap<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_reader_split) // XML ismi doğru olmalı

        currentBook = intent.getSerializableExtra("BOOK_DATA") as? Book
        if (currentBook == null) {
            Toast.makeText(this, "Hata: Kitap verisi yok!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        prepareInterleavedFlow()
        loadContent(currentIndex)

        btnNext.setOnClickListener { handleNextClick() }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadContent(currentIndex)
            }
        }

        btnClose.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Çıkış")
                .setMessage("Okumayı bitirmek istiyor musun?")
                .setPositiveButton("Evet") { _, _ -> finish() }
                .setNegativeButton("Hayır", null)
                .show()
        }
    }

    private fun initViews() {
        // ID'ler XML ile birebir aynı olmalı
        layoutStory = findViewById(R.id.layoutStoryContainer)
        layoutQuestion = findViewById(R.id.layoutQuestionContainer)
        pdfViewStory = findViewById(R.id.pdfViewStory)
        pdfViewQuestion = findViewById(R.id.pdfViewQuestion)
        progressBarStory = findViewById(R.id.progressBarStory)

        tvInfo = findViewById(R.id.tvStepInfo)

        // Hata veren butonlar burada:
        btnNext = findViewById(R.id.btnReaderNext)
        btnPrev = findViewById(R.id.btnReaderPrev)
        btnClose = findViewById(R.id.btnCloseReader)

        tvResult = findViewById(R.id.tvResult)

        btnA = findViewById(R.id.btnOptA)
        btnB = findViewById(R.id.btnOptB)
        btnC = findViewById(R.id.btnOptC)
        btnD = findViewById(R.id.btnOptD)
    }

    private fun prepareInterleavedFlow() {
        flowList.clear()
        val stories = currentBook!!.storyUrls
        val questions = currentBook!!.questions
        val maxSize = maxOf(stories.size, questions.size)

        for (i in 0 until maxSize) {
            if (i < stories.size) flowList.add(Pair("STORY", i))
            if (i < questions.size) flowList.add(Pair("QUESTION", i))
        }
    }

    private fun loadContent(index: Int) {
        if (index >= flowList.size) return

        // Geri butonu ayarı
        btnPrev.isEnabled = index > 0
        btnPrev.alpha = if (index > 0) 1.0f else 0.5f

        val type = flowList[index].first
        val originalIndex = flowList[index].second

        if (type == "STORY") {
            layoutStory.visibility = View.VISIBLE
            layoutQuestion.visibility = View.GONE
            tvInfo.text = "Hikaye: Bölüm ${originalIndex + 1}"
            btnNext.text = "İLERİ (SORU)"

            val url = currentBook!!.storyUrls[originalIndex]
            loadPdfFromUrl(url, pdfViewStory, progressBarStory)

        } else {
            layoutStory.visibility = View.GONE
            layoutQuestion.visibility = View.VISIBLE
            tvInfo.text = "Soru ${originalIndex + 1}"
            btnNext.text = "İLERİ (DEVAM)"

            val questionData = currentBook!!.questions[originalIndex]

            // SORU YÜKLEME MANTIĞI (Kilitli mi Açık mı?)
            loadQuestionUI(questionData, originalIndex)
        }
    }

    private fun loadQuestionUI(q: Question, originalIndex: Int) {
        loadPdfFromUrl(q.questionPdfUrl, pdfViewQuestion)

        tvResult.text = ""
        val buttons = listOf(btnA, btnB, btnC, btnD)

        // Bu soru daha önce çözüldü mü?
        val savedAnswerIndex = answeredQuestions[originalIndex]

        buttons.forEachIndexed { index, btn ->
            if (index < q.options.size) {
                btn.visibility = View.VISIBLE
                btn.text = q.options[index]
                btn.setTextColor(Color.BLACK)

                if (savedAnswerIndex != null) {
                    // --- DAHA ÖNCE ÇÖZÜLMÜŞ! ---
                    btn.isEnabled = false // Tıklamayı kapat

                    if (index == q.correctOptionIndex) {
                        btn.setBackgroundColor(Color.parseColor("#4CAF50")) // Doğru (Yeşil)
                    } else if (index == savedAnswerIndex) {
                        btn.setBackgroundColor(Color.parseColor("#F44336")) // Yanlış (Kırmızı)
                    } else {
                        btn.setBackgroundColor(Color.LTGRAY) // Diğerleri Gri
                    }

                    // Sonucu Yazdır
                    if (savedAnswerIndex == q.correctOptionIndex) {
                        tvResult.text = "DOĞRU! (Daha önce çözüldü)"
                        tvResult.setTextColor(Color.parseColor("#4CAF50"))
                    } else {
                        tvResult.text = "YANLIŞ (Daha önce çözüldü)"
                        tvResult.setTextColor(Color.parseColor("#F44336"))
                    }

                } else {
                    // --- HENÜZ ÇÖZÜLMEMİŞ ---
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
        // Cevabı kaydet (Böylece geri gelince hatırlayacak)
        answeredQuestions[questionIndex] = selectedIndex

        buttons.forEach { it.isEnabled = false } // Butonları kilitle

        if (selectedIndex == correctIndex) {
            buttons[selectedIndex].setBackgroundColor(Color.parseColor("#4CAF50"))
            tvResult.text = "DOĞRU! 👏"
            tvResult.setTextColor(Color.parseColor("#4CAF50"))
            totalCorrect++
        } else {
            buttons[selectedIndex].setBackgroundColor(Color.parseColor("#F44336"))
            buttons[correctIndex].setBackgroundColor(Color.parseColor("#4CAF50")) // Doğruyu göster
            tvResult.text = "YANLIŞ 😔"
            tvResult.setTextColor(Color.parseColor("#F44336"))
        }
    }

    private fun handleNextClick() {
        val currentType = flowList[currentIndex].first
        val questionIndex = flowList[currentIndex].second

        // --- YENİ EKLENEN KONTROL ---
        // Eğer şu an ekranda SORU varsa VE bu soru cevaplanmışlar listesinde yoksa:
        if (currentType == "QUESTION" && !answeredQuestions.containsKey(questionIndex)) {
            Toast.makeText(this, "Lütfen devam etmeden önce soruyu cevaplayınız!", Toast.LENGTH_SHORT).show()
            return // Fonksiyonu burada durdur, ilerlemesine izin verme
        }

        // Buraya geldiyse ya hikayedir ya da soru çözülmüştür, devam et
        if (currentIndex < flowList.size - 1) {
            currentIndex++
            loadContent(currentIndex)
        } else {
            // Kitap Bitti
            AlertDialog.Builder(this)
                .setTitle("Tebrikler! 🎉")
                .setMessage("Kitabı tamamladınız!\nDoğru Sayısı: $totalCorrect")
                .setPositiveButton("Bitir") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun loadPdfFromUrl(url: String, pdfView: PDFView, progressBar: ProgressBar? = null) {
        progressBar?.visibility = View.VISIBLE
        var fixedUrl = url
        if (fixedUrl.contains("github.com") && fixedUrl.contains("/blob/")) {
            fixedUrl = fixedUrl.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(fixedUrl).openStream()
                withContext(Dispatchers.Main) {
                    pdfView.fromStream(inputStream)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .onLoad { progressBar?.visibility = View.GONE }
                        .onError { progressBar?.visibility = View.GONE }
                        .load()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}