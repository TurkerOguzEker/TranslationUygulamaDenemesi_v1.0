package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Language

class LanguageAdapter(
    private val languages: List<Language>,
    private val currentLanguageCode: String,
    private val onLanguageSelected: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bind(language, currentLanguageCode, onLanguageSelected)
    }

    override fun getItemCount(): Int = languages.size

    class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLanguageName: TextView = itemView.findViewById(R.id.tvLanguageName)
        private val ivLanguageSelected: ImageView = itemView.findViewById(R.id.ivLanguageSelected)

        fun bind(language: Language, currentLanguageCode: String, onLanguageSelected: (String) -> Unit) {
            tvLanguageName.text = language.name
            ivLanguageSelected.visibility = if (language.code == currentLanguageCode) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                onLanguageSelected(language.code)
            }
        }
    }
}