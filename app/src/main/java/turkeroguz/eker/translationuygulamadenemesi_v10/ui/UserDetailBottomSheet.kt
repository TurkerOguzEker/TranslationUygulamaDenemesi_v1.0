package turkeroguz.eker.translationuygulamadenemesi_v10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.R
import turkeroguz.eker.translationuygulamadenemesi_v10.model.User

class UserDetailBottomSheet(private val user: User) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_user_detail_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvDetailName).text = user.name
        view.findViewById<TextView>(R.id.tvDetailEmail).text = user.email
        view.findViewById<TextView>(R.id.tvDetailUid).text = "UID: ${user.uid}"

        val switchPremium = view.findViewById<MaterialSwitch>(R.id.switchPremiumDetail)
        val switchBan = view.findViewById<MaterialSwitch>(R.id.switchBan)

        // Veritabanı verisini switchlere yansıt
        switchPremium.isChecked = user.isPremium
        switchBan.isChecked = user.isBanned

        // Canlı güncelleme
        switchPremium.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(user.uid).update("isPremium", isChecked)
        }

        switchBan.setOnCheckedChangeListener { _, isChecked ->
            db.collection("users").document(user.uid).update("isBanned", isChecked)
        }

        view.findViewById<Button>(R.id.btnCloseSheet).setOnClickListener { dismiss() }
    }
}