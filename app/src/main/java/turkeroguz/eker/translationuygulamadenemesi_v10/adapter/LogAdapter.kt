package turkeroguz.eker.translationuygulamadenemesi_v10.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.ItemLogBinding // Eğer viewbinding yoksa manuel view kullanacağız
import turkeroguz.eker.translationuygulamadenemesi_v10.model.ActivityLog
import java.text.SimpleDateFormat
import java.util.*

// Not: ViewBinding kullanmıyorsanız aşağıda view.findViewById ile yapın, burada standart binding örneği veriyorum.
// Eğer ItemLogBinding hata verirse XML dosyasını oluşturduktan sonra Build -> Rebuild yapın.
class LogAdapter(private val logs: List<ActivityLog>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.binding.tvDescription.text = log.description
        holder.binding.tvDate.text = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(log.timestamp))

        // Log tipine göre renk kodlaması
        when (log.actionType) {
            "ERROR", "BAN" -> holder.binding.tvActionType.setTextColor(Color.RED)
            "PURCHASE", "PREMIUM" -> holder.binding.tvActionType.setTextColor(Color.parseColor("#FFD700")) // Altın
            "LOGIN" -> holder.binding.tvActionType.setTextColor(Color.BLUE)
            else -> holder.binding.tvActionType.setTextColor(Color.GRAY)
        }
        holder.binding.tvActionType.text = log.actionType
    }

    override fun getItemCount() = logs.size
}