package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
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
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class BookReaderActivity : AppCompatActivity() {

    private lateinit var layoutStory: LinearLayout
    private lateinit var layoutQuestion: LinearLayout

    // Modern PDF Görüntüleyiciler
    private lateinit var pdfViewStory: PDFView
    private lateinit var pdfViewQuestion: PDFView
    private lateinit var progressBarStory: ProgressBar

    private lateinit var tvInfo: TextView
    private lateinit var btnNext: Button
    private lateinit var tvResult: TextView

    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button

    private var currentBook: Book? = null
    private val flowList = ArrayList<Pair<String, Int>>()
    private var currentIndex = 0
    private var totalCorrect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_reader_split)

        // Veriyi al
        currentBook = intent.getSerializableExtra("BOOK_DATA") as? Book
        if (currentBook == null) {
            Toast.makeText(this, "Hata: Kitap verisi bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        prepareFlow()
        loadContent(currentIndex)

        btnNext.setOnClickListener {
            if (flowList.isNotEmpty() && flowList[currentIndex].first == "QUESTION" && tvResult.text.isEmpty()) {
                Toast.makeText(this, "Lütfen bir şık seçin!", Toast.LENGTH_SHORT).show()
            } else {
                moveNext()
            }
        }
    }

    private fun initViews() {
        layoutStory = findViewById(R.id.layoutStoryContainer)
        layoutQuestion = findViewById(R.id.layoutQuestionContainer)
        pdfViewStory = findViewById(R.id.pdfViewStory)
        pdfViewQuestion = findViewById(R.id.pdfViewQuestion)
        progressBarStory = findViewById(R.id.progressBarStory)

        tvInfo = findViewById(R.id.tvStepInfo)
        btnNext = findViewById(R.id.btnReaderNext)
        tvResult = findViewById(R.id.tvResult)

        btnA = findViewById(R.id.btnOptA)
        btnB = findViewById(R.id.btnOptB)
        btnC = findViewById(R.id.btnOptC)
        btnD = findViewById(R.id.btnOptD)
    }

    private fun prepareFlow() {
        val maxLen = maxOf(currentBook!!.storyUrls.size, currentBook!!.questions.size)
        for (i in 0 until maxLen) {
            if (i < currentBook!!.storyUrls.size) flowList.add(Pair("STORY", i))
            if (i < currentBook!!.questions.size) flowList.add(Pair("QUESTION", i))
        }
    }

    private fun loadContent(index: Int) {
        if (index >= flowList.size) return

        val type = flowList[index].first
        val listIndex = flowList[index].second

        if (type == "STORY") {
            // --- HİKAYE MODU ---
            layoutStory.visibility = View.VISIBLE
            layoutQuestion.visibility = View.GONE

            tvInfo.text = "${currentBook?.title} - ${listIndex + 1}. Bölüm"
            btnNext.text = "Soruyu Çöz"

            val rawUrl = currentBook!!.storyUrls[listIndex]
            loadPdfFromUrl(rawUrl, pdfViewStory, progressBarStory)

        } else {
            // --- SORU MODU ---
            layoutStory.visibility = View.GONE
            layoutQuestion.visibility = View.VISIBLE

            tvInfo.text = "Soru Zamanı! (${listIndex + 1})"
            btnNext.text = if (index == flowList.size - 1) "Bitir" else "Sonraki Bölüm"

            val questionData = currentBook!!.questions[listIndex]
            loadQuestionUI(questionData)
        }
    }

    // --- MODERN PDF YÜKLEME FONKSİYONU ---
    private fun loadPdfFromUrl(url: String, pdfView: PDFView, progressBar: ProgressBar? = null) {
        progressBar?.visibility = View.VISIBLE

        // Link Düzeltme (GitHub Pages için gerekmez ama yedek olsun)
        var fixedUrl = url
        if (fixedUrl.contains("github.com") && fixedUrl.contains("/blob/")) {
            fixedUrl = fixedUrl.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")
        }

        // Arka Planda İndir, Ön Planda Göster
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(fixedUrl).openStream()
                withContext(Dispatchers.Main) {
                    pdfView.fromStream(inputStream)
                        .enableSwipe(true) // Kaydırma Açık
                        .swipeHorizontal(false) // Dikey Kaydırma (Kitap gibi)
                        .enableDoubletap(true) // Çift tıkla zoom
                        .defaultPage(0)
                        .onLoad {
                            progressBar?.visibility = View.GONE
                        }
                        .onError {
                            Toast.makeText(this@BookReaderActivity, "PDF Hatası: ${it.message}", Toast.LENGTH_SHORT).show()
                            progressBar?.visibility = View.GONE
                        }
                        .load()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookReaderActivity, "Yükleme Hatası. İnternetinizi kontrol edin.", Toast.LENGTH_SHORT).show()
                    progressBar?.visibility = View.GONE
                }
                e.printStackTrace()
            }
        }
    }

    private fun loadQuestionUI(q: Question) {
        loadPdfFromUrl(q.questionPdfUrl, pdfViewQuestion)

        tvResult.text = ""
        val buttons = listOf(btnA, btnB, btnC, btnD)

        for (i in buttons.indices) {
            val btn = buttons[i]
            btn.isEnabled = true
            btn.setBackgroundColor(Color.parseColor("#E0E0E0")) // Gri (Varsayılan)
            btn.setTextColor(Color.BLACK)

            if (i < q.options.size) {
                btn.visibility = View.VISIBLE
                btn.text = q.options[i]
                btn.setOnClickListener { checkAnswer(i, q.correctOptionIndex, buttons) }
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    private fun checkAnswer(selectedIndex: Int, correctIndex: Int, buttons: List<Button>) {
        buttons.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            buttons[selectedIndex].setBackgroundColor(Color.parseColor("#4CAF50")) // Yeşil
            tvResult.text = "Doğru Cevap! 🎉"
            tvResult.setTextColor(Color.parseColor("#4CAF50"))
            totalCorrect++
        } else {
            buttons[selectedIndex].setBackgroundColor(Color.parseColor("#F44336")) // Kırmızı
            buttons[correctIndex].setBackgroundColor(Color.parseColor("#4CAF50")) // Yeşil
            tvResult.text = "Yanlış Cevap 😔"
            tvResult.setTextColor(Color.parseColor("#F44336"))
        }
    }

    private fun moveNext() {
        if (currentIndex < flowList.size - 1) {
            currentIndex++
            loadContent(currentIndex)
        } else {
            AlertDialog.Builder(this)
                .setTitle("Tebrikler!")
                .setMessage("Kitabı tamamladınız!\nSkorunuz: $totalCorrect")
                .setPositiveButton("Tamam") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }
}