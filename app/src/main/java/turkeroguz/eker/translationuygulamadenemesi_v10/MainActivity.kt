package turkeroguz.eker.translationuygulamadenemesi_v10



import android.content.res.Configuration

import android.graphics.Bitmap

import android.graphics.Canvas

import android.graphics.Color

import android.graphics.Paint

import android.graphics.Typeface

import android.os.Bundle

import android.view.View

import android.widget.ImageView

import android.widget.LinearLayout

import android.widget.TextView

import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.app.AppCompatDelegate

import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide

import com.google.android.material.bottomsheet.BottomSheetDialog

import com.google.android.material.materialswitch.MaterialSwitch

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore

import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.LanguageAdapter

import turkeroguz.eker.translationuygulamadenemesi_v10.model.Language

import java.util.Locale

import turkeroguz.eker.translationuygulamadenemesi_v10.ui.LoginFragment

import turkeroguz.eker.translationuygulamadenemesi_v10.ui.RegisterFragment

import turkeroguz.eker.translationuygulamadenemesi_v10.ui.AdminPanelFragment



class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)



// NOT: Profil butonuna tıklama olayı artık HomeFragment içinde (btnProfile) yönetiliyor.

// Bu yüzden burada ayrıca bir tanımlama yapmanıza gerek yok.



        if (savedInstanceState == null) {

            checkUserAndNavigate()

        }



// Alt Menü Butonları

        findViewById<View>(R.id.btnHome).setOnClickListener {

            replaceFragment(HomeFragment())

        }



        findViewById<View>(R.id.btnSearch).setOnClickListener {

            replaceFragment(BooksFragment())

        }



        findViewById<View>(R.id.btnMyBooks).setOnClickListener {

            replaceFragment(MyBooksFragment())

        }



        findViewById<View>(R.id.btnWords).setOnClickListener {

            replaceFragment(WordsFragment())

        }



        findViewById<View>(R.id.btnSettings).setOnClickListener {

            replaceFragment(SettingsFragment())

        }

    }



// --- Profil Dialogu İçin Yardımcı Fonksiyonlar ---



// Mail adresinden isim türeten yardımcı fonksiyon

    private fun getNameFromEmail(email: String): String {

        return if (email.contains("@")) {

            email.substringBefore("@")

                .replace(".", " ")

                .split(" ")

                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

        } else {

            "Kullanıcı"

        }

    }



// Harften Resim (Bitmap) Oluşturan Fonksiyon (Dialog içindeki resim için gerekli)

    private fun createProfileBitmap(text: String): Bitmap {

        val width = 200

        val height = 200

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val paint = Paint()



// Arka plan rengi (Koyu Mavi/Mor tonu)

        paint.color = Color.parseColor("#5C6BC0")

        paint.style = Paint.Style.FILL

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)



// Harf ayarları

        paint.color = Color.WHITE

        paint.textSize = 100f

        paint.textAlign = Paint.Align.CENTER

        paint.typeface = Typeface.DEFAULT_BOLD



// Harfi tam ortaya hizalama

        val xPos = (canvas.width / 2).toFloat()

        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)



        canvas.drawText(text, xPos, yPos, paint)



        return bitmap

    }



// --- Navigasyon Fonksiyonları ---



    fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()

            .replace(R.id.fragment_container, fragment)

            .commit()

    }



    fun navigateToBooksSearch(query: String) {

        val fragment = BooksFragment().apply {

            arguments = Bundle().apply {

                putString("search_query", query)

            }

        }

        replaceFragment(fragment)

        findViewById<View>(R.id.btnSearch).performClick()

    }



// --- Profil Penceresi (Bottom Sheet) ---



    fun showProfileDialog() {

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(R.layout.layout_profile_sheet, null)

        dialog.setContentView(view)



        val user = FirebaseAuth.getInstance().currentUser



// XML ID Eşleştirmeleri

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)

        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)

        val tvId = view.findViewById<TextView>(R.id.tvProfileId)

        val ivSheetProfile = view.findViewById<ImageView>(R.id.ivProfileImage)



        if (user != null) {

            val email = user.email ?: ""

            val nameFromEmail = getNameFromEmail(email)



// Verileri Yazdır

            tvName?.text = nameFromEmail

            tvEmail?.text = email

            tvId?.text = "ID: ${user.uid}"



// Profil Resmini Yükle (Dialog içindeki)

            if (ivSheetProfile != null) {

                if (user.photoUrl != null) {

                    Glide.with(this)

                        .load(user.photoUrl)

                        .circleCrop()

                        .into(ivSheetProfile)

                } else {

// Resim yoksa harf oluştur

                    val initial = nameFromEmail.firstOrNull()?.toString()?.uppercase() ?: "?"

                    val letterBitmap = createProfileBitmap(initial)



                    Glide.with(this)

                        .load(letterBitmap)

                        .circleCrop()

                        .into(ivSheetProfile)

                }

            }

        } else {

            tvName?.text = "Misafir"

            tvEmail?.text = ""

            tvId?.text = "Giriş Yapılmadı"

        }



// --- Tema ve Diğer Ayarlar ---

        val themeSwitch = view.findViewById<MaterialSwitch>(R.id.themeSwitch)

        val themeIcon = view.findViewById<ImageView>(R.id.ivThemeIcon)

        val themeSwitchContainer = view.findViewById<LinearLayout>(R.id.themeSwitchContainer)



        themeSwitchContainer?.setOnClickListener {

            themeSwitch.isChecked = !themeSwitch.isChecked

        }



        themeSwitch?.let {

            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            it.isChecked = isNightMode

            if (isNightMode) themeIcon?.setImageResource(android.R.drawable.ic_menu_recent_history)



            it.setOnCheckedChangeListener { _, isChecked ->

                AppCompatDelegate.setDefaultNightMode(

                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

                )

            }

        }



        view.findViewById<View>(R.id.btnClose)?.setOnClickListener { dialog.dismiss() }



// ÇIKIŞ YAP Butonu

        view.findViewById<View>(R.id.btnSheetLogout)?.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            dialog.dismiss()

            checkUserAndNavigate()

            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()

        }



        view.findViewById<View>(R.id.tvSelectLanguage)?.setOnClickListener {

            showLanguageSelectionDialog(it)

        }



        dialog.show()

    }



    fun showLanguageSelectionDialog(view: View) {

        val dialog = BottomSheetDialog(this)

        val dialogView = layoutInflater.inflate(R.layout.dialog_language_selection, null)

        dialog.setContentView(dialogView)



        val languages = listOf(Language("English", "en"), Language("Türkçe", "tr"))

        val currentLanguageCode = resources.configuration.locales[0].language



        val rvLanguages = dialogView.findViewById<RecyclerView>(R.id.rvLanguages)

        rvLanguages.layoutManager = LinearLayoutManager(this)

        rvLanguages.adapter = LanguageAdapter(languages, currentLanguageCode) { languageCode ->

            setLocale(languageCode)

            dialog.dismiss()

        }

        dialog.show()

    }



    private fun setLocale(languageCode: String) {

        val locale = Locale(languageCode)

        Locale.setDefault(locale)

        val config = Configuration()

        config.setLocale(locale)

        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        recreate()

    }



    fun checkUserAndNavigate() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            replaceFragment(LoginFragment())

        } else {

            FirebaseFirestore.getInstance().collection("users").document(user.uid)

                .get()

                .addOnSuccessListener { document ->

                    if (isFinishing || isDestroyed) return@addOnSuccessListener

                    if (document.exists()) {

                        val role = document.getString("role")

                        if (role == "admin") {

                            Toast.makeText(this, "Yönetici Girişi", Toast.LENGTH_SHORT).show()

                        }

                    }

                    replaceFragment(HomeFragment())

// updateMainProfileImage() ÇAĞRISI SİLİNDİ.

                }

                .addOnFailureListener {

                    if (!isFinishing && !isDestroyed) replaceFragment(HomeFragment())

                }

        }

    }

}