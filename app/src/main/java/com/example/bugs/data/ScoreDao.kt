package com.example.bugs.data

import androidx.room.*

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreRecordEntity): Long

    @Update
    suspend fun updateScore(score: ScoreRecordEntity)

    @Query("SELECT * FROM score_records ORDER BY score DESC, timestamp DESC")
    suspend fun getAllScores(): List<ScoreRecordEntity>

    @Query("SELECT * FROM score_records WHERE difficulty = :difficulty ORDER BY score DESC, timestamp DESC")
    suspend fun getScoresByDifficulty(difficulty: Int): List<ScoreRecordEntity>

    @Query("SELECT * FROM score_records WHERE userId = :userId AND difficulty = :difficulty LIMIT 1")
    suspend fun getScoreForUserAndDifficulty(userId: Long, difficulty: Int): ScoreRecordEntity?
}
