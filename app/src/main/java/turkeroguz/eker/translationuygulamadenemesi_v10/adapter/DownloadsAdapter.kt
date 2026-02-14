package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class DownloadsAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvBookTitle)
        val image: ImageView = view.findViewById(R.id.imgBookCover)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_downloaded_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title

        // Resim yükleme: Cihaz çevrimdışı olsa bile önbellekten veya placeholder'dan gösterir
        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .placeholder(R.drawable.ic_book) // Yüklenirken görünecek ikon
            .error(R.drawable.ic_book)       // Hata durumunda görünecek ikon
            .into(holder.image)

        // Tüm karta tıklandığında (DownloadsFragment'tan gelen mantığı çalıştırır)
        holder.itemView.setOnClickListener {
            onBookClick(book)
        }

        // Sadece silme butonuna tıklandığında
        holder.btnDelete.setOnClickListener {
            onDeleteClick(book)
        }
    }

    override fun getItemCount() = books.size

    // Liste güncellendiğinde RecyclerView'ı yenilemek için
    fun updateList(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}