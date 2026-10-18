package com.example.bugs.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.data.ScoreRecordEntity

class RecordsAdapter(
    private var records: List<ScoreRecordEntity>
) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tvRank)
        val tvUserName: TextView = view.findViewById(R.id.tvRecordUserName)
        val tvDetails: TextView = view.findViewById(R.id.tvRecordDetails)
        val tvDate: TextView = view.findViewById(R.id.tvRecordDate)
        val tvScore: TextView = view.findViewById(R.id.tvRecordScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.tvRank.text = (position + 1).toString()
        holder.tvUserName.text = record.userName
        holder.tvDetails.text = "Сложность: ${record.difficultyName} | Промахи: ${record.misses}"
        holder.tvDate.text = record.date
        holder.tvScore.text = record.score.toString()
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<ScoreRecordEntity>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
