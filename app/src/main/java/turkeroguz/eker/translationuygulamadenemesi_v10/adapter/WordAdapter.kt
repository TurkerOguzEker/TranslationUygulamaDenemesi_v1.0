package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Word

class WordAdapter(
    private val words: List<Word>,
    private val onToggleLearned: (Word) -> Unit,
    private val onDelete: (Word) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    inner class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEnglish: TextView = view.findViewById(R.id.tvWordEnglish)
        val tvTurkish: TextView = view.findViewById(R.id.tvWordTurkish)
        val btnToggleLearned: ImageButton = view.findViewById(R.id.btnToggleLearned)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteWord)
        val viewLearnedBar: View = view.findViewById(R.id.viewLearnedBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.tvEnglish.text = word.english
        holder.tvTurkish.text = word.turkish

        val green = ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
        val gray = ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)

        val color = if (word.isLearned) green else gray
        holder.viewLearnedBar.setBackgroundColor(color)
        holder.btnToggleLearned.setColorFilter(color)

        holder.btnToggleLearned.setOnClickListener { onToggleLearned(word) }
        holder.btnDelete.setOnClickListener { onDelete(word) }
    }

    override fun getItemCount() = words.size
}
