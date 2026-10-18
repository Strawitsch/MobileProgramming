package com.example.bugs.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.bugs.R

class SettingsFragment : Fragment() {

    private var isApplyingPreset = false

    private data class DifficultyPreset(
        val speed: Int,
        val maxBugs: Int,
        val bonusInterval: Int,
        val roundDuration: Int
    )

    private val presets = listOf(
        DifficultyPreset(2, 6, 20, 60),   // Очень легкий
        DifficultyPreset(4, 8, 18, 60),   // Легкий
        DifficultyPreset(6, 10, 15, 60),  // Средний
        DifficultyPreset(8, 12, 12, 45),  // Сложный
        DifficultyPreset(10, 15, 10, 45)  // Очень сложный
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerDifficulty = view.findViewById<Spinner>(R.id.spinnerSettingsDifficulty)
        val seekSpeed = view.findViewById<SeekBar>(R.id.seekSpeed)
        val tvSpeedValue = view.findViewById<TextView>(R.id.tvSpeedValue)
        val etMaxBugs = view.findViewById<EditText>(R.id.etMaxBugs)
        val etBonusInterval = view.findViewById<EditText>(R.id.etBonusInterval)
        val etRoundDuration = view.findViewById<EditText>(R.id.etRoundDuration)
        val btnSave = view.findViewById<Button>(R.id.btnSaveSettings)

        val difficultyLevels = resources.getStringArray(R.array.difficulty_levels_array)
        spinnerDifficulty.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            difficultyLevels
        )

        val prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val savedDifficulty = prefs.getInt("settings_difficulty_index", 2) // default Medium

        seekSpeed.max = 9
        val savedSpeed = prefs.getInt("speed", 6)
        seekSpeed.progress = (savedSpeed - 1).coerceIn(0, 9)
        tvSpeedValue.text = getString(R.string.speed, savedSpeed)

        etMaxBugs.setText(prefs.getInt("max_bugs", 10).toString())
        etBonusInterval.setText(prefs.getInt("bonus_interval", 15).toString())
        etRoundDuration.setText(prefs.getInt("round_duration", 60).toString())

        isApplyingPreset = true
        spinnerDifficulty.setSelection(savedDifficulty.coerceIn(0, 5))
        isApplyingPreset = false

        spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isApplyingPreset) return
                if (position in presets.indices) {
                    val preset = presets[position]
                    isApplyingPreset = true
                    seekSpeed.progress = (preset.speed - 1).coerceIn(0, 9)
                    tvSpeedValue.text = getString(R.string.speed, preset.speed)
                    etMaxBugs.setText(preset.maxBugs.toString())
                    etBonusInterval.setText(preset.bonusInterval.toString())
                    etRoundDuration.setText(preset.roundDuration.toString())
                    isApplyingPreset = false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSpeedValue.text = getString(R.string.speed1, progress + 1)
                if (fromUser && !isApplyingPreset) {
                    spinnerDifficulty.setSelection(5) // Custom (Пользовательская)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isApplyingPreset) {
                    spinnerDifficulty.setSelection(5) // Custom (Пользовательская)
                }
            }
        }

        etMaxBugs.addTextChangedListener(textWatcher)
        etBonusInterval.addTextChangedListener(textWatcher)
        etRoundDuration.addTextChangedListener(textWatcher)

        btnSave.setOnClickListener {
            val speed = seekSpeed.progress + 1
            val maxBugs = (etMaxBugs.text.toString().toIntOrNull() ?: 10).coerceIn(1, 50)
            val bonusInterval = (etBonusInterval.text.toString().toIntOrNull() ?: 15).coerceIn(1, 60)
            val roundDuration = (etRoundDuration.text.toString().toIntOrNull() ?: 60).coerceIn(5, 300)
            val difficultyIndex = spinnerDifficulty.selectedItemPosition

            etMaxBugs.setText(maxBugs.toString())
            etBonusInterval.setText(bonusInterval.toString())
            etRoundDuration.setText(roundDuration.toString())

            prefs.edit {
                putInt("speed", speed)
                    .putInt("max_bugs", maxBugs)
                    .putInt("bonus_interval", bonusInterval)
                    .putInt("round_duration", roundDuration)
                    .putInt("settings_difficulty_index", difficultyIndex)
            }

            Toast.makeText(context, getString(R.string.pref_created_toast), Toast.LENGTH_SHORT).show()
        }
    }
}
