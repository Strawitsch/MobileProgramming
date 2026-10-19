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
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsFragment : Fragment() {

    private lateinit var rvRecords: RecyclerView
    private lateinit var tvEmptyRecords: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: RecordsAdapter

    private var currentFilterDifficulty: Int = -1 // -1 means all

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
        tabLayout = view.findViewById(R.id.tabLayoutDifficulties)

        rvRecords.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecordsAdapter(emptyList())
        rvRecords.adapter = adapter

        setupTabs()
        loadRecords(currentFilterDifficulty)
    }

    override fun onResume() {
        super.onResume()
        loadRecords(currentFilterDifficulty)
    }

    private fun setupTabs() {
        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("Все"))
        val difficultyLevels = resources.getStringArray(R.array.difficulty_levels_array)
        for (level in difficultyLevels) {
            tabLayout.addTab(tabLayout.newTab().setText(level))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                currentFilterDifficulty = if (position == 0) -1 else position - 1
                loadRecords(currentFilterDifficulty)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadRecords(difficultyFilter: Int) {
        lifecycleScope.launch {
            val ctx = context ?: return@launch
            val scores = withContext(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(ctx).scoreDao()
                if (difficultyFilter == -1) {
                    dao.getAllScores()
                } else {
                    dao.getScoresByDifficulty(difficultyFilter)
                }
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
