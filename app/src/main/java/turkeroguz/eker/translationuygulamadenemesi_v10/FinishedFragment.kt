package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.BookAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.databinding.FragmentFinishedBinding
import turkeroguz.eker.translationuygulamadenemesi_v10.model.Book
import turkeroguz.eker.translationuygulamadenemesi_v10.ui.BookDetailBottomSheet

class FinishedFragment : Fragment() {

    private var _binding: FragmentFinishedBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val finishedBooksList = ArrayList<Book>()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinishedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Liste Tasarımı (Alt alta sıralı)
        binding.rvFinished.layoutManager = LinearLayoutManager(requireContext())

        // Adapter Bağlantısı ve Tıklama Olayı
        adapter = BookAdapter(finishedBooksList) { selectedBook ->
            // Kitaba tıklandığında detay penceresini aç
            val detailSheet = BookDetailBottomSheet(selectedBook)
            detailSheet.show(parentFragmentManager, "BookDetailSheet")
        }
        binding.rvFinished.adapter = adapter

        // Verileri Çek
        loadFinishedBooks()
    }

    private fun loadFinishedBooks() {
        val uid = auth.currentUser?.uid ?: return

        // addSnapshotListener kullanarak veriyi canlı (realtime) çekiyoruz.
        // Böylece kitap %100 olduğunda liste anında güncellenir.
        db.collection("users").document(uid).collection("finished_books")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    finishedBooksList.clear()
                    for (doc in snapshot.documents) {
                        val book = doc.toObject(Book::class.java)
                        if (book != null) {
                            book.bookId = doc.id // ID'yi garantiye alıyoruz
                            finishedBooksList.add(book)
                        }
                    }
                    adapter.notifyDataSetChanged()

                    // Liste boşsa uyarı yazısını göster, doluysa gizle ve listeyi göster
                    if (finishedBooksList.isEmpty()) {
                        binding.tvEmptyFinished.visibility = View.VISIBLE
                        binding.rvFinished.visibility = View.GONE
                    } else {
                        binding.tvEmptyFinished.visibility = View.GONE
                        binding.rvFinished.visibility = View.VISIBLE
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}