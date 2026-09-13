package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.GuruConsultationEntity
import com.example.data.local.entity.TuningHistoryEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TuningHistoryDao {
    @Query("SELECT * FROM tuning_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<TuningHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TuningHistoryEntity): Long

    @Query("DELETE FROM tuning_history")
    suspend fun clearAll()
}

@Dao
interface GuruConsultationDao {
    @Query("SELECT * FROM guru_consultations ORDER BY timestamp DESC")
    fun getAllConsultations(): Flow<List<GuruConsultationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GuruConsultationEntity): Long

    @Query("DELETE FROM guru_consultations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM guru_consultations")
    suspend fun clearAll()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getProfile(userId: String = "primary_user"): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)
}
