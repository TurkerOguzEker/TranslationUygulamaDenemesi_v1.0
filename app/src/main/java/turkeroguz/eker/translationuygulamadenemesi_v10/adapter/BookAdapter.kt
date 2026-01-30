package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemBookCardBinding // DÜZELTME 1: Yeni Binding sınıfı
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class BookAdapter(
    private val books: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // DÜZELTME 2: ItemBookCardBinding inflate ediliyor (item_book_card.xml)
        val binding = ItemBookCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    // ViewHolder sınıfında da yeni Binding kullanılıyor
    inner class BookViewHolder(private val binding: ItemBookCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            // DÜZELTME 3: txtBookName -> txtBookTitle (Yeni ID)
            binding.txtBookTitle.text = book.title

            // DÜZELTME 4: Yazar/Kategori kısmını da dolduralım
            binding.txtBookAuthor.text = if (book.level.isNotEmpty()) book.level else "Genel"

            // Resmi Glide ile yükle
            if (book.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.imageUrl)
                    .placeholder(R.drawable.ic_book)
                    .error(R.drawable.ic_book)
                    .into(binding.imgBookCover)
            } else {
                binding.imgBookCover.setImageResource(R.drawable.ic_book)
            }

            // Tıklama olayı
            binding.root.setOnClickListener {
                onClick(book)
            }
        }
    }
}