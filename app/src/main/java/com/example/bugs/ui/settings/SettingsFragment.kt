package com.example.bugs.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugs.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seekSpeed = view.findViewById<SeekBar>(R.id.seekSpeed)
        val etMaxBugs = view.findViewById<EditText>(R.id.etMaxBugs)
        val etBonusInterval = view.findViewById<EditText>(R.id.etBonusInterval)
        val etRoundDuration = view.findViewById<EditText>(R.id.etRoundDuration)
        val btnSave = view.findViewById<Button>(R.id.btnSaveSettings)

        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

        seekSpeed.progress = prefs.getInt("speed", 5)
        etMaxBugs.setText(prefs.getInt("max_bugs", 10).toString())
        etBonusInterval.setText(prefs.getInt("bonus_interval", 15).toString())
        etRoundDuration.setText(prefs.getInt("round_duration", 60).toString())

        btnSave.setOnClickListener {
            val maxBugs = etMaxBugs.text.toString().toIntOrNull() ?: 10
            val bonusInterval = etBonusInterval.text.toString().toIntOrNull() ?: 15
            val roundDuration = etRoundDuration.text.toString().toIntOrNull() ?: 60

            prefs.edit()
                .putInt("speed", seekSpeed.progress)
                .putInt("max_bugs", maxBugs)
                .putInt("bonus_interval", bonusInterval)
                .putInt("round_duration", roundDuration)
                .apply()

            Toast.makeText(context, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }
    }
}