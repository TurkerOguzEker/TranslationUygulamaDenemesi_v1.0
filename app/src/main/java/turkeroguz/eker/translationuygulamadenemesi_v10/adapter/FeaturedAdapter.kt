package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemBookBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.FeaturedBook

class FeaturedAdapter(
    private val books: List<FeaturedBook>,
    private val useSearchLayout: Boolean = false // Buraya bu parametreyi ekledik
) : RecyclerView.Adapter<FeaturedAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: FeaturedBook) {
            binding.imgBookCover.setImageResource(book.imageRes)
            binding.txtBookName.text = book.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // EĞER ARAMA SAYFASINDAYSAK:
        // ViewPager hatasını önlemek için XML'de match_parent olan yüksekliği,
        // burada programatik olarak wrap_content'e çeviriyoruz.
        if (useSearchLayout) {
            val params = binding.root.layoutParams
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            binding.root.layoutParams = params
        }

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size
}