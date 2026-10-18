package com.example.bugs.ui.navigation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R

class NavAdapter(
    private var items: List<NavItem>,
    private val onItemClick: (NavItem) -> Unit
) : RecyclerView.Adapter<NavAdapter.NavViewHolder>() {

    class NavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivNavIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvNavTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nav, parent, false)
        return NavViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.ivIcon.setImageResource(item.iconResId)

        if (item.isSelected) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E0F2FE"))
            holder.tvTitle.setTextColor(Color.parseColor("#0284C7"))
            holder.ivIcon.setColorFilter(Color.parseColor("#0284C7"))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.tvTitle.setTextColor(Color.parseColor("#333333"))
            holder.ivIcon.setColorFilter(Color.parseColor("#666666"))
        }

        holder.itemView.setOnClickListener {
            setSelected(position)
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setSelected(position: Int) {
        items.forEachIndexed { index, item ->
            item.isSelected = (index == position)
        }
        notifyDataSetChanged()
    }
}
