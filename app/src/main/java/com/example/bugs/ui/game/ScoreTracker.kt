package com.example.bugs.ui.game

class ScoreTracker {
    var score: Int = 0
        private set
    var misses: Int = 0
        private set

    fun addKill(points: Int = 10) {
        score += points
    }

    fun addMiss() {
        misses++
    }

    fun reset() {
        score = 0
        misses = 0
    }
}