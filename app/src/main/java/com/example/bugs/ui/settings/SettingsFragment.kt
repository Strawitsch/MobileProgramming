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
import androidx.lifecycle.lifecycleScope
import com.example.bugs.R
import com.example.bugs.data.AppDatabase
import com.example.bugs.model.game.DifficultyPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var isApplyingPreset = false

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

        seekSpeed.max = 14
        val savedSpeed = prefs.getInt("speed", 10)
        seekSpeed.progress = (savedSpeed - 1).coerceIn(0, 14)
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
                if (position in DifficultyPresets.presets.indices) {
                    val preset = DifficultyPresets.presets[position]
                    isApplyingPreset = true
                    seekSpeed.progress = (preset.speed - 1).coerceIn(0, 14)
                    tvSpeedValue.text = getString(R.string.speed, preset.speed)
                    etMaxBugs.setText(preset.maxBugs.toString())
                    etBonusInterval.setText(preset.bonusInterval.toString())
                    etRoundDuration.setText(preset.roundDuration.toString())
                    isApplyingPreset = false

                    val ctx = context
                    if (ctx != null) {
                        DifficultyPresets.applyPreset(ctx, position)
                        lifecycleScope.launch(Dispatchers.IO) {
                            AppDatabase.getInstance(ctx).userDao().updateActiveUserDifficulty(position)
                        }
                    }
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

            val ctx = context
            if (ctx != null && difficultyIndex in DifficultyPresets.presets.indices) {
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance(ctx).userDao().updateActiveUserDifficulty(difficultyIndex)
                }
            }

            Toast.makeText(context, getString(R.string.pref_created_toast), Toast.LENGTH_SHORT).show()
        }
    }
}
