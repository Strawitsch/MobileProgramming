package com.example.bugs.ui.authors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.model.Author

class AuthorsAdapter(private val authors: List<Author>) : RecyclerView.Adapter<AuthorsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivAuthorPhoto)
        val tvName: TextView = view.findViewById(R.id.tvAuthorName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_author, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val author = authors[position]
        holder.tvName.text = author.name
        holder.ivPhoto.setImageResource(author.photoResId)
    }

    override fun getItemCount() = authors.size
}