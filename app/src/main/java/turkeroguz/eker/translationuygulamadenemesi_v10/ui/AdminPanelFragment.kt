package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class AdminPanelFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etLevel: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var etPdfUrl: EditText
    private lateinit var btnAdd: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View Elemanlarını Tanımla
        etTitle = view.findViewById(R.id.etBookTitle)
        etLevel = view.findViewById(R.id.etBookLevel)
        etImageUrl = view.findViewById(R.id.etImageUrl)
        etPdfUrl = view.findViewById(R.id.etPdfUrl)
        btnAdd = view.findViewById(R.id.btnAddBook)

        btnAdd.setOnClickListener {
            saveBookToFirebase()
        }
    }

    private fun saveBookToFirebase() {
        val title = etTitle.text.toString().trim()
        val level = etLevel.text.toString().trim().uppercase() // A1, b2 -> A1, B2 olsun
        val imageUrl = etImageUrl.text.toString().trim()
        val pdfUrl = etPdfUrl.text.toString().trim()

        // Basit doğrulama
        if (title.isEmpty() || level.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(context, "Lütfen başlık, seviye ve resim alanlarını doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        // Butonu kilitle (Çift tıklamayı önle)
        btnAdd.isEnabled = false
        btnAdd.text = "Kaydediliyor..."

        // Yeni kitap nesnesi oluştur
        // ID'yi Firebase otomatik verecek, şimdilik boş gönderiyoruz
        val newBook = Book(
            bookId = "",
            title = title,
            level = level,
            imageUrl = imageUrl,
            pdfUrl = pdfUrl
        )

        // Firestore'a Ekle
        FirebaseFirestore.getInstance().collection("books")
            .add(newBook)
            .addOnSuccessListener { documentReference ->
                // Başarılı olursa ID'yi güncelle (Opsiyonel ama iyi olur)
                documentReference.update("bookId", documentReference.id)

                Toast.makeText(context, "Kitap Başarıyla Eklendi!", Toast.LENGTH_LONG).show()
                clearFields()

                btnAdd.isEnabled = true
                btnAdd.text = "Kitabı Kaydet"
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                btnAdd.isEnabled = true
                btnAdd.text = "Kitabı Kaydet"
            }
    }

    private fun clearFields() {
        etTitle.text.clear()
        etLevel.text.clear()
        etImageUrl.text.clear()
        etPdfUrl.text.clear()
    }
}