package com.example.bugs.data

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("UPDATE users SET isSelected = (id = :selectedId)")
    suspend fun setSelectedUser(selectedId: Long)

    @Query("SELECT * FROM users WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedUser(): UserEntity?

    @Query("UPDATE users SET difficulty = :difficulty WHERE isSelected = 1")
    suspend fun updateActiveUserDifficulty(difficulty: Int)
}
