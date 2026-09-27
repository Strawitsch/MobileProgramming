package com.example.bugs.ui.authors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.model.Author

class AuthorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_authors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAuthors = view.findViewById<RecyclerView>(R.id.rvAuthors)
        rvAuthors.layoutManager = LinearLayoutManager(context)

        // Пример списка авторов. Замените drawable на свои изображения
        val authors = listOf(
            Author("Иванов Иван", R.drawable.ic_launcher_background),
            Author("Петров Петр", R.drawable.ic_launcher_background),
            Author("Сидорова Анна", R.drawable.ic_launcher_background)
        )

        rvAuthors.adapter = AuthorsAdapter(authors)
    }
}