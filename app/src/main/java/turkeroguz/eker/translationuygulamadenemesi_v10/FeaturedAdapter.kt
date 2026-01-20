package turkeroguz.eker.translationuygulamadenemesi_v10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeaturedAdapter(private val books: List<FeaturedBook>) :
    RecyclerView.Adapter<FeaturedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imgBookCover)
        val titleView: TextView = view.findViewById(R.id.txtBookName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.titleView.text = book.title
        holder.imageView.setImageResource(book.imageRes)
    }

    override fun getItemCount() = books.size
}