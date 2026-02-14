package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentFavoritesBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.BookDetailBottomSheet

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var adapter: BookAdapter
    private val favoriteBooks = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        // Projendeki mevcut BookAdapter'ı kullanıyoruz
        adapter = BookAdapter(favoriteBooks) { selectedBook ->
            // Kitaba tıklandığında detay sayfasını aç
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetail")
        }

        // Kitapları yan yana 2'li ızgara şeklinde diziyoruz (Ana sayfadaki gibi)
        binding.rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvFavorites.adapter = adapter
    }

    private fun loadFavorites() {
        val uid = auth.currentUser?.uid ?: return

        // Firebase'deki "favorites" koleksiyonunu canlı (realtime) olarak dinle
        db.collection("users").document(uid).collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                favoriteBooks.clear()

                if (snapshot != null && !snapshot.isEmpty) {
                    // Firebase'den verileri al listeye ekle
                    for (document in snapshot.documents) {
                        val book = document.toObject(Book::class.java)
                        if (book != null) {
                            favoriteBooks.add(book)
                        }
                    }

                    binding.rvFavorites.visibility = View.VISIBLE
                    binding.tvEmptyFavorites.visibility = View.GONE
                } else {
                    // Favori yoksa "Henüz favori kitabınız bulunmuyor" yazısını göster
                    binding.rvFavorites.visibility = View.GONE
                    binding.tvEmptyFavorites.visibility = View.VISIBLE
                }

                // Listeyi yenile
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Bellek sızıntısını önlemek için
    }
}