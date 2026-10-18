package com.example.bugs.model.game

import android.content.Context
import androidx.core.content.edit

data class DifficultyPreset(
    val speed: Int,
    val maxBugs: Int,
    val bonusInterval: Int,
    val roundDuration: Int
)

object DifficultyPresets {
    val presets = listOf(
        DifficultyPreset(4, 6, 20, 60),   // 0: Очень легкий
        DifficultyPreset(7, 8, 18, 60),   // 1: Легкий
        DifficultyPreset(10, 10, 15, 60), // 2: Средний
        DifficultyPreset(13, 12, 12, 45), // 3: Сложный
        DifficultyPreset(15, 15, 10, 45)  // 4: Очень сложный
    )

    fun applyPreset(context: Context, difficultyIndex: Int) {
        if (difficultyIndex !in presets.indices) return
        val preset = presets[difficultyIndex]
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putInt("speed", preset.speed)
                .putInt("max_bugs", preset.maxBugs)
                .putInt("bonus_interval", preset.bonusInterval)
                .putInt("round_duration", preset.roundDuration)
                .putInt("settings_difficulty_index", difficultyIndex)
                .putInt("player_difficulty", difficultyIndex)
        }
    }
}
