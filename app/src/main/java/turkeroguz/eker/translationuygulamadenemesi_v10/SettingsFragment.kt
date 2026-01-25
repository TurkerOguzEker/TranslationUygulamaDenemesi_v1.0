package turkeroguz.eker.translationuygulamadenemesi_v10

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentSettingsBinding
import java.util.Calendar
import turkeroguz.eker.translationuygulamadenemesi_v10.BuildConfig

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnReminderTime.setOnClickListener {
            showTimePickerDialog()
        }

        binding.btnPremium.setOnClickListener {
            Snackbar.make(view, "Premium özellikleri yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnLearningLanguage.setOnClickListener {
            Snackbar.make(view, "Dil seçimi özelliği yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnTranslationLanguage.setOnClickListener {
            Snackbar.make(view, "Dil seçimi özelliği yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            Snackbar.make(view, "Hesap silme özelliği yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnCustomerSupport.setOnClickListener {
            Snackbar.make(view, "Müşteri desteği yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnRateUs.setOnClickListener {
            Snackbar.make(view, "Bizi oyladığınız için teşekkürler!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnShareApp.setOnClickListener {
            val appPackageName = requireActivity().packageName
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Uygulamamıza göz atın: https://play.google.com/store/apps/details?id=$appPackageName")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            Snackbar.make(view, "Gizlilik politikası yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnTermsOfUse.setOnClickListener {
            Snackbar.make(view, "Kullanım şartları yakında!", Snackbar.LENGTH_SHORT).show()
        }

        binding.switchSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "açıldı" else "kapatıldı"
            Snackbar.make(view, "Ses efektleri $status", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnSuggestImprovement.setOnClickListener {
            Snackbar.make(view, "Öneriniz için teşekkürler!", Snackbar.LENGTH_SHORT).show()
        }

        binding.txtAppVersion.text = BuildConfig.VERSION_NAME
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.btnReminderTime.text = time
            setReminder(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun setReminder(hour: Int, minute: Int) {
        val context = requireContext().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Snackbar.make(binding.root, "Hatırlatma $hour:$minute için kuruldu!", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
