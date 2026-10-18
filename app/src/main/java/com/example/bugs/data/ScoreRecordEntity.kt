package com.example.bugs.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_records")
data class ScoreRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val score: Int,
    val misses: Int,
    val difficulty: Int,
    val difficultyName: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
