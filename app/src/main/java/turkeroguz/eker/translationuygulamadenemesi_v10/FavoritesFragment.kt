package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.BookDetailBottomSheet

class FavoritesFragment : Fragment() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var tvNoFavorites: TextView
    private lateinit var adapter: BookAdapter
    private val favoriteList = ArrayList<Book>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFavorites = view.findViewById(R.id.rvFavorites)
        tvNoFavorites = view.findViewById(R.id.tvNoFavorites)

        rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = BookAdapter(favoriteList) { selectedBook ->
            val bottomSheet = BookDetailBottomSheet(selectedBook)
            bottomSheet.show(parentFragmentManager, "BookDetail")
        }
        rvFavorites.adapter = adapter

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                favoriteList.clear()

                try {
                    for (doc in documents) {
                        val book = doc.toObject(Book::class.java)
                        // Bazen bookId Firestore'a boş kaydedilebilir, ID'yi manuel de verelim garanti olsun
                        book.bookId = doc.id
                        favoriteList.add(book)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Veri dönüştürme hatası!", Toast.LENGTH_SHORT).show()
                }

                adapter.notifyDataSetChanged()

                if (favoriteList.isEmpty()) {
                    tvNoFavorites.visibility = View.VISIBLE
                    rvFavorites.visibility = View.GONE
                } else {
                    tvNoFavorites.visibility = View.GONE
                    rvFavorites.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Favoriler yüklenemedi!", Toast.LENGTH_SHORT).show()
            }
    }
}