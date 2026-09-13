package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tuning_history")
data class TuningHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val instrumentId: String,
    val instrumentName: String,
    val stringName: String,
    val targetHz: Float,
    val detectedHz: Float,
    val centsOffset: Float,
    val inTune: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "guru_consultations")
data class GuruConsultationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userQuestion: String,
    val guruAnswer: String,
    val instrumentName: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String = "primary_user",
    val displayName: String = "Musician",
    val email: String = "guest@stringtuner.app",
    val isGoogleConnected: Boolean = false,
    val activeInstrumentId: String = "guitar_standard",
    val joinedTimestamp: Long = System.currentTimeMillis()
)
