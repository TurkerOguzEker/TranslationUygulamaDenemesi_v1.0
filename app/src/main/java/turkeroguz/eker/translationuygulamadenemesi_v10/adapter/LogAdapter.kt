package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemLogBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.ActivityLog
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(private val logList: List<ActivityLog>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]

        // --- DÜZELTME BURADA YAPILDI ---
        // Modelimizdeki 'action' verisini tasarımdaki 'tvActionType' kutusuna yazıyoruz
        holder.binding.tvActionType.text = log.action

        // Modelimizdeki 'details' verisini tasarımdaki 'tvDescription' kutusuna yazıyoruz
        holder.binding.tvDescription.text = log.details

        // Tarih Formatı (Firebase Timestamp -> Date)
        val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        holder.binding.tvDate.text = sdf.format(log.timestamp.toDate())

        // Renklendirme (Log tipine göre)
        val color = when(log.type) {
            "success" -> Color.parseColor("#4CAF50") // Yeşil
            "error" -> Color.parseColor("#F44336")   // Kırmızı
            "warning" -> Color.parseColor("#FFC107") // Sarı
            else -> Color.parseColor("#2196F3")      // Mavi
        }
        holder.binding.tvActionType.setTextColor(color)
    }

    override fun getItemCount() = logList.size
}