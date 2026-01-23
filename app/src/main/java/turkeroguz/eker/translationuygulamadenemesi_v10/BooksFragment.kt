package turkeroguz.eker.translationuygulamadenemesi_v10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import turkeroguz.eker.translationuygulamadenemesi_v10.adapter.FeaturedAdapter
import turkeroguz.eker.translationuygulamadenemesi_v10.model.FeaturedBook

class BooksFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)

        val levels = resources.getStringArray(R.array.levels_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        val autoCompleteTextView: AutoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)

        val featuredList = listOf(
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background),
            FeaturedBook("Adventure", R.drawable.ic_launcher_background),
            FeaturedBook("Grammar", R.drawable.ic_launcher_background),
            FeaturedBook("Mystery", R.drawable.ic_launcher_background)
        )

        val rvBooks: RecyclerView = view.findViewById(R.id.rvBooks)
        rvBooks.layoutManager = GridLayoutManager(context, 2)
        rvBooks.adapter = FeaturedAdapter(featuredList)

        return view
    }
}