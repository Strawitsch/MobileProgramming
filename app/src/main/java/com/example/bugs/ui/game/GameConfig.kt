package com.example.bugs.ui.game

import android.content.Context

/**
 * TODO: Проверить работоспособность данного класса. Проверить подзагрузку из SharedPreferences
 */

data class GameConfig(
    val speed: Int,           // 1..10
    val maxBugs: Int,         // 1..50
    val bonusInterval: Int,   // 1..60 секунд
    val roundDuration: Int,   // 5..300 секунд
    val hammerDuration: Int,  // 3..10 секунд (бонус)
    val waypointsMin: Int,    // 3
    val waypointsMax: Int     // 6
) {

    // Задержка между спавнами жуков (мс): 1000..100
    fun spawnDelayMs(): Long {
        val normalized = (speed - 1) / 9f
        return (1000L - 900L * normalized).toLong()
    }

    // Длительность одного сегмента A до B (мс): 2000..500
    fun segmentDurationMs(): Long {
        val normalized = (speed - 1) / 9f
        return (2000L - 1500L * normalized).toLong()
    }

    companion object {
        fun load(context: Context): GameConfig {
            val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            return GameConfig(
                speed = prefs.getInt("speed", 5).coerceIn(1, 10),
                maxBugs = prefs.getInt("max_bugs", 10).coerceIn(1, 50),
                bonusInterval = prefs.getInt("bonus_interval", 15).coerceIn(1, 60),
                roundDuration = prefs.getInt("round_duration", 60).coerceIn(5, 300),
                hammerDuration = prefs.getInt("hammer_duration", 5).coerceIn(3, 10),
                waypointsMin = prefs.getInt("waypoints_min", 3).coerceIn(1, 10),
                waypointsMax = prefs.getInt("waypoints_max", 6).coerceIn(1, 20)
            )
        }
    }
}