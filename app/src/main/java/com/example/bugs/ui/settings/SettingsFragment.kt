package com.example.bugs.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
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
        val tvSpeedValue = view.findViewById<TextView>(R.id.tvSpeedValue)
        val etMaxBugs = view.findViewById<EditText>(R.id.etMaxBugs)
        val etBonusInterval = view.findViewById<EditText>(R.id.etBonusInterval)
        val etRoundDuration = view.findViewById<EditText>(R.id.etRoundDuration)
        val btnSave = view.findViewById<Button>(R.id.btnSaveSettings)

        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

        seekSpeed.max = 9
        seekSpeed.progress = (prefs.getInt("speed", 5) - 1).coerceIn(0, 9)
        tvSpeedValue.text = "Скорость: ${seekSpeed.progress + 1}"

        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSpeedValue.text = "Скорость: ${progress + 1}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        etMaxBugs.setText(prefs.getInt("max_bugs", 10).toString())
        etBonusInterval.setText(prefs.getInt("bonus_interval", 15).toString())
        etRoundDuration.setText(prefs.getInt("round_duration", 60).toString())

        btnSave.setOnClickListener {
            val speed = seekSpeed.progress + 1
            val maxBugs = (etMaxBugs.text.toString().toIntOrNull() ?: 10).coerceIn(1, 50)
            val bonusInterval = (etBonusInterval.text.toString().toIntOrNull() ?: 15).coerceIn(1, 60)
            val roundDuration = (etRoundDuration.text.toString().toIntOrNull() ?: 60).coerceIn(5, 300)

            etMaxBugs.setText(maxBugs.toString())
            etBonusInterval.setText(bonusInterval.toString())
            etRoundDuration.setText(roundDuration.toString())

            prefs.edit()
                .putInt("speed", speed)
                .putInt("max_bugs", maxBugs)
                .putInt("bonus_interval", bonusInterval)
                .putInt("round_duration", roundDuration)
                .apply()

            Toast.makeText(context, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }
    }
}