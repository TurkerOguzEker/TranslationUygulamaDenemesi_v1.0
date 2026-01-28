package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemBookBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book

class BookAdapter(
    private val books: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            // Kitap ismini yaz
            binding.txtBookName.text = book.title

            // Resmi Glide ile yükle
            if (book.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(book.imageUrl)
                    .placeholder(R.drawable.ic_book) // Yüklenirken gösterilecek resim
                    .error(R.drawable.ic_book)       // Hata olursa gösterilecek resim
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