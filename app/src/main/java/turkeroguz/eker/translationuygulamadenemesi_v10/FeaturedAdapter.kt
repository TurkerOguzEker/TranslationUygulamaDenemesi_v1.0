package turkeroguz.eker.translationuygulamadenemesi_v10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeaturedAdapter(private val books: List<FeaturedBook>) :
    RecyclerView.Adapter<FeaturedAdapter.ViewHolder>() {

    // ViewHolder sınıfı FeaturedAdapter sınıfının içinde olmalı
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.bookImage)
        val titleView: TextView = view.findViewById(R.id.bookTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // R.layout.item_book dosyasının var olduğundan emin ol
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.titleView.text = book.title
        // book.imageRes FeaturedBook dosyasında tanımlı olmalı
        holder.imageView.setImageResource(book.imageRes)
    }

    override fun getItemCount() = books.size
}