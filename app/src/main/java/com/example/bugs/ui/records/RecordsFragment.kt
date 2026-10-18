package com.example.bugs.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsFragment : Fragment() {

    private lateinit var rvRecords: RecyclerView
    private lateinit var tvEmptyRecords: TextView
    private lateinit var adapter: RecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvRecords = view.findViewById(R.id.rvRecords)
        tvEmptyRecords = view.findViewById(R.id.tvEmptyRecords)

        rvRecords.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecordsAdapter(emptyList())
        rvRecords.adapter = adapter

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val ctx = context ?: return@launch
            val scores = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(ctx).scoreDao().getAllScores()
            }
            if (!isAdded) return@launch

            if (scores.isEmpty()) {
                tvEmptyRecords.visibility = View.VISIBLE
                rvRecords.visibility = View.GONE
            } else {
                tvEmptyRecords.visibility = View.GONE
                rvRecords.visibility = View.VISIBLE
                adapter.updateRecords(scores)
            }
        }
    }
}
