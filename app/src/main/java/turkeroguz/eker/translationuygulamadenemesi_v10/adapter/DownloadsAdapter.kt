package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class DownloadsAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    // Firebase bağlantıları eklendi
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvBookTitle)
        val level: TextView = view.findViewById(R.id.tvBookLevel)
        val image: ImageView = view.findViewById(R.id.imgBookCover)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteBook)

        // YENİ EKLENENLER: Çember ve Yüzde yazısı
        val pbProgress: CircularProgressIndicator = view.findViewById(R.id.pbBookProgress)
        val tvProgress: TextView = view.findViewById(R.id.tvBookProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_downloaded_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title

        // Kitap seviyesinin dinamik olarak atanması
        if (book.level.isNotEmpty()) {
            holder.level.text = "Seviye ${book.level}"
        } else {
            holder.level.text = "Genel Seviye"
        }

        // Resim yükleme
        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .placeholder(R.drawable.ic_book)
            .error(R.drawable.ic_book)
            .into(holder.image)

        // --- İLERLEME YÜZDESİNİ ÇEKME ---
        // Önce varsayılan olarak %0 göster
        holder.pbProgress.progress = 0
        holder.tvProgress.text = "%0"
        holder.pbProgress.setIndicatorColor(Color.parseColor("#FF9800")) // Turuncu

        val uid = auth.currentUser?.uid
        if (uid != null && book.bookId.isNotEmpty()) {
            db.collection("users").document(uid).collection("book_progress").document(book.bookId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val progressInt = doc.getLong("progress")?.toInt() ?: 0
                        holder.pbProgress.progress = progressInt
                        holder.tvProgress.text = "%$progressInt"

                        // %100 olunca rengi yeşil yap
                        if (progressInt == 100) {
                            holder.pbProgress.setIndicatorColor(Color.parseColor("#4CAF50"))
                        }
                    }
                }
        }

        // Tıklama olayları
        holder.itemView.setOnClickListener {
            onBookClick(book)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(book)
        }
    }

    override fun getItemCount() = books.size

    fun updateList(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}