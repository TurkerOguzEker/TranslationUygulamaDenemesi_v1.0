package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemBookCardBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class BookAdapter(
    private val books: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(private val binding: ItemBookCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.txtBookTitle.text = book.title
            binding.txtBookAuthor.text = if (book.level.isNotEmpty()) book.level else "Genel"

            // GITHUB SADELEŞTİRMESİ:
            // Artık link düzeltme koduna gerek yok. GitHub 'Raw' linki doğrudan çalışır.
            if (book.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.imageUrl)
                    .placeholder(R.drawable.ic_book)
                    .error(R.drawable.ic_book)
                    .into(binding.imgBookCover)
            } else {
                binding.imgBookCover.setImageResource(R.drawable.ic_book)
            }

            binding.root.setOnClickListener {
                onClick(book)
            }
        }
    }
}