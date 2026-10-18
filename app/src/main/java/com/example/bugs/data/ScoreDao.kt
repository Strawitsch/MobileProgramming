package com.example.bugs.data

import androidx.room.*

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreRecordEntity): Long

    @Query("SELECT * FROM score_records ORDER BY score DESC, timestamp DESC")
    suspend fun getAllScores(): List<ScoreRecordEntity>

    @Query("SELECT * FROM score_records WHERE userId = :userId ORDER BY score DESC")
    suspend fun getScoresForUser(userId: Long): List<ScoreRecordEntity>
}
