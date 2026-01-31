package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Question

class BookReaderActivity : AppCompatActivity() {

    // Görünümler
    private lateinit var webViewContainer: LinearLayout // Hikaye alanı
    private lateinit var questionContainer: LinearLayout // Soru alanı

    private lateinit var webView: WebView // Hikaye için PDF Okuyucu
    private lateinit var tvInfo: TextView
    private lateinit var btnNext: Button

    // Soru Görünümleri
    private lateinit var wvQuestion: WebView // YENİ: Soru için PDF Okuyucu
    private lateinit var btnOptA: Button
    private lateinit var btnOptB: Button
    private lateinit var btnOptC: Button
    private lateinit var btnOptD: Button
    private lateinit var tvResult: TextView

    private var currentBook: Book? = null

    // Akış Listesi: Pair("STORY", index) veya Pair("QUESTION", index)
    private val flowList = ArrayList<Pair<String, Int>>()
    private var currentIndex = 0
    private var totalCorrect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_reader_split)

        // Kitap verisini al
        currentBook = intent.getSerializableExtra("BOOK_DATA") as? Book
        if (currentBook == null) {
            Toast.makeText(this, "Kitap verisi alınamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- GÖRÜNÜMLERİ BAĞLA ---
        webViewContainer = findViewById(R.id.layoutStoryContainer)
        questionContainer = findViewById(R.id.layoutQuestionContainer)
        webView = findViewById(R.id.webViewReader)
        tvInfo = findViewById(R.id.tvStepInfo)
        btnNext = findViewById(R.id.btnReaderNext)

        // Soru bileşenleri
        wvQuestion = findViewById(R.id.wvQuestion) // YENİ: Soru WebView
        btnOptA = findViewById(R.id.btnOptA)
        btnOptB = findViewById(R.id.btnOptB)
        btnOptC = findViewById(R.id.btnOptC)
        btnOptD = findViewById(R.id.btnOptD)
        tvResult = findViewById(R.id.tvResult)

        // WebView Ayarları (Hikaye)
        setupWebView(webView)

        // WebView Ayarları (Soru)
        setupWebView(wvQuestion)

        // Akışı hazırla ve başlat
        prepareFlow()
        loadContent(currentIndex)

        btnNext.setOnClickListener {
            // Eğer soru ekranındaysak ve cevap verilmediyse uyar
            if (flowList.isNotEmpty() && flowList[currentIndex].first == "QUESTION" && tvResult.text.isEmpty()) {
                Toast.makeText(this, "Lütfen bir cevap seçin!", Toast.LENGTH_SHORT).show()
            } else {
                moveToNext()
            }
        }
    }

    private fun setupWebView(wv: WebView) {
        wv.settings.javaScriptEnabled = true
        wv.webViewClient = WebViewClient()
    }

    private fun prepareFlow() {
        // Sıra: Hikaye 1 -> Soru 1 -> Hikaye 2 -> Soru 2 ...
        val maxLen = maxOf(currentBook!!.storyUrls.size, currentBook!!.questions.size)
        for (i in 0 until maxLen) {
            if (i < currentBook!!.storyUrls.size) flowList.add(Pair("STORY", i))
            if (i < currentBook!!.questions.size) flowList.add(Pair("QUESTION", i))
        }
    }

    private fun loadContent(index: Int) {
        if (index >= flowList.size) return

        val type = flowList[index].first
        val dataIndex = flowList[index].second

        if (type == "STORY") {
            // --- HİKAYE MODU ---
            webViewContainer.visibility = View.VISIBLE
            questionContainer.visibility = View.GONE

            val url = currentBook!!.storyUrls[dataIndex]
            val driveUrl = "https://docs.google.com/gview?embedded=true&url=$url"
            webView.loadUrl(driveUrl)

            tvInfo.text = "Hikaye Okuma (${dataIndex + 1}. Bölüm)"
            btnNext.text = "Soruyu Çöz"

        } else {
            // --- SORU MODU ---
            webViewContainer.visibility = View.GONE
            questionContainer.visibility = View.VISIBLE

            val question = currentBook!!.questions[dataIndex]
            loadQuestion(question)

            tvInfo.text = "Test Zamanı (${dataIndex + 1}. Soru)"
            btnNext.text = if (index == flowList.size - 1) "Bitir" else "Sonraki Bölüm"
        }
    }

    private fun loadQuestion(q: Question) {
        // YENİ: Soru metni yerine PDF Linkini WebView'e yükle
        val driveUrl = "https://docs.google.com/gview?embedded=true&url=${q.questionPdfUrl}"
        wvQuestion.loadUrl(driveUrl)

        tvResult.text = "" // Sonucu temizle

        // Şık butonlarını ayarla
        val options = listOf(btnOptA, btnOptB, btnOptC, btnOptD)
        for (i in 0..3) {
            if (i < q.options.size) {
                options[i].text = q.options[i]
                options[i].visibility = View.VISIBLE
                options[i].isEnabled = true
                options[i].setBackgroundColor(Color.WHITE) // Rengi sıfırla

                // Tıklama Olayı
                options[i].setOnClickListener {
                    checkAnswer(i, q.correctOptionIndex, options)
                }
            } else {
                options[i].visibility = View.GONE
            }
        }
    }

    private fun checkAnswer(selectedIndex: Int, correctIndex: Int, buttons: List<Button>) {
        // Tüm butonları kilitle (Bir kere cevap verilebilir)
        buttons.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            // DOĞRU
            buttons[selectedIndex].setBackgroundColor(Color.GREEN)
            tvResult.text = "Doğru Cevap! 🎉"
            tvResult.setTextColor(Color.GREEN)
            totalCorrect++
        } else {
            // YANLIŞ
            buttons[selectedIndex].setBackgroundColor(Color.RED)
            buttons[correctIndex].setBackgroundColor(Color.GREEN) // Doğruyu da göster
            tvResult.text = "Yanlış Cevap 😔"
            tvResult.setTextColor(Color.RED)
        }
    }

    private fun moveToNext() {
        if (currentIndex < flowList.size - 1) {
            currentIndex++
            loadContent(currentIndex)
        } else {
            showFinalResult()
        }
    }

    private fun showFinalResult() {
        AlertDialog.Builder(this)
            .setTitle("Tebrikler!")
            .setMessage("Kitabı bitirdiniz.\nSkorunuz: $totalCorrect / ${currentBook!!.questions.size}")
            .setPositiveButton("Tamam") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}