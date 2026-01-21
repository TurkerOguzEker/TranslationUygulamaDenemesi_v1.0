package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.FeaturedBook

class FeaturedAdapter(private val books: List<FeaturedBook>) :
    RecyclerView.Adapter<FeaturedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // XML'deki id: imgBookCover
        val imageView: ImageView = view.findViewById(R.id.imgBookCover)
        // XML'deki id: txtBookTitle (Burayı düzelttik)
        val titleView: TextView = view.findViewById(R.id.txtBookTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // item_book.xml dosyasını bağlıyoruz
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