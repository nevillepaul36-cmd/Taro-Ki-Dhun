package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.ai.GuruEngine
import com.example.data.local.AppDatabase
import com.example.data.local.entity.GuruConsultationEntity
import com.example.data.local.entity.TuningHistoryEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val tuningHistoryDao = db.tuningHistoryDao()
    private val guruDao = db.guruConsultationDao()
    private val profileDao = db.userProfileDao()

    val tuningHistory: Flow<List<TuningHistoryEntity>> = tuningHistoryDao.getAllHistory()
    val guruConsultations: Flow<List<GuruConsultationEntity>> = guruDao.getAllConsultations()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getProfile()

    suspend fun recordTuningSuccess(
        instrumentId: String,
        instrumentName: String,
        stringName: String,
        targetHz: Float,
        detectedHz: Float,
        centsOffset: Float
    ) {
        val entry = TuningHistoryEntity(
            instrumentId = instrumentId,
            instrumentName = instrumentName,
            stringName = stringName,
            targetHz = targetHz,
            detectedHz = detectedHz,
            centsOffset = centsOffset,
            inTune = true,
            timestamp = System.currentTimeMillis()
        )
        tuningHistoryDao.insert(entry)
    }

    suspend fun askGuru(
        question: String,
        instrumentName: String,
        imageUri: Uri?
    ): String {
        val answer = GuruEngine.consultGuru(context, question, instrumentName, imageUri)
        val consultation = GuruConsultationEntity(
            userQuestion = question,
            guruAnswer = answer,
            instrumentName = instrumentName,
            imageUri = imageUri?.toString(),
            timestamp = System.currentTimeMillis()
        )
        guruDao.insert(consultation)
        return answer
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        profileDao.saveProfile(profile)
    }

    suspend fun clearHistory() {
        tuningHistoryDao.clearAll()
    }

    suspend fun deleteConsultation(id: Long) {
        guruDao.deleteById(id)
    }
}
