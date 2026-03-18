package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class BookAdapter(
    private var bookList: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun updateBooks(newBooks: List<Book>) {
        this.bookList = newBooks
        notifyDataSetChanged()
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.imgBookCover)
        val tvTitle: TextView = itemView.findViewById(R.id.txtBookTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.txtBookAuthor)

        // Sağ üst köşedeki ilerleme grubu (FrameLayout)
        val layoutProgress: FrameLayout = itemView.findViewById(R.id.layoutProgress)
        val pbProgress: CircularProgressIndicator = itemView.findViewById(R.id.pbBookProgress)
        val tvProgress: TextView = itemView.findViewById(R.id.tvBookProgress)

        // GÜNCELLENDİ: Kartın başlık alanındaki okunma sayısı dairesi içindeki yazı
        val tvReadCount: TextView = itemView.findViewById(R.id.tvCardReadCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_card, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author ?: "Yazar Bilinmiyor"

        if (book.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(book.imageUrl).into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_launcher_background)
        }

        // --- OKUNMA SAYISINI FORMATLAMA (0.1k, 1k vs.) ---
        holder.tvReadCount.text = formatReadCount(book.readCount)

        // BAŞLANGIÇTA YÜZDELİK VE ÇEMBER GİZLİ (Okunmadıysa tertemiz durur)
        holder.layoutProgress.visibility = View.GONE

        val uid = auth.currentUser?.uid
        if (uid != null && book.bookId.isNotEmpty()) {
            db.collection("users").document(uid).collection("book_progress").document(book.bookId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val progressInt = doc.getLong("progress")?.toInt() ?: 0

                        // Sadece okumaya başlandıysa ilerleme dairesini ve yüzdeyi göster
                        if (progressInt > 0) {
                            holder.layoutProgress.visibility = View.VISIBLE

                            holder.pbProgress.progress = progressInt
                            holder.tvProgress.text = "%$progressInt"

                            if (progressInt == 100) {
                                holder.pbProgress.setIndicatorColor(Color.parseColor("#4CAF50")) // Bitenler yeşil
                            } else {
                                holder.pbProgress.setIndicatorColor(Color.parseColor("#FF9800")) // Devam edenler turuncu
                            }
                        }
                    }
                }
        }

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    // SAYIYI "k" FORMATINA ÇEVİREN FONKSİYON (Örn: 900 -> 0.9k)
    private fun formatReadCount(count: Int): String {
        return if (count >= 100) {
            val kValue = count / 1000.0
            // Noktadan sonra 1 basamak göster (0.1, 0.9, 1.5 gibi)
            val formatted = String.format(java.util.Locale.US, "%.1f", kValue)

            // Eğer "1.0k" çıkarsa, onu "1k" yapalım
            if (formatted.endsWith(".0")) {
                "${formatted.replace(".0", "")}k"
            } else {
                "${formatted}k"
            }
        } else {
            // 100'den küçükse direkt sayıyı yaz (Örn: 45)
            count.toString()
        }
    }
}