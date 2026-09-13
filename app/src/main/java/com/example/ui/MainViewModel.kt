package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PitchDetector
import com.example.audio.PitchResult
import com.example.audio.ToneGenerator
import com.example.data.local.entity.GuruConsultationEntity
import com.example.data.local.entity.TuningHistoryEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.Instrument
import com.example.data.model.InstrumentCatalog
import com.example.data.model.InstrumentString
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2

enum class MainTab(val title: String) {
    TUNER("Tuner"),
    GURU("Guru AI"),
    INSTRUMENTS("Instruments"),
    PROFILE("Profile")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)
    private val pitchDetector = PitchDetector()

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(MainTab.TUNER)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Instruments
    val allInstruments: List<Instrument> = InstrumentCatalog.instruments
    private val _selectedInstrument = MutableStateFlow(InstrumentCatalog.instruments.first())
    val selectedInstrument: StateFlow<Instrument> = _selectedInstrument.asStateFlow()

    // Selected String
    private val _selectedString = MutableStateFlow<InstrumentString?>(selectedInstrument.value.strings.firstOrNull())
    val selectedString: StateFlow<InstrumentString?> = _selectedString.asStateFlow()

    // Auto String Detection Mode (GuitarTuna style: Auto vs Manual)
    private val _isAutoDetectMode = MutableStateFlow(true)
    val isAutoDetectMode: StateFlow<Boolean> = _isAutoDetectMode.asStateFlow()

    // Auto Sound Listener Enabled
    private val _isAutoSoundListener = MutableStateFlow(true)
    val isAutoSoundListener: StateFlow<Boolean> = _isAutoSoundListener.asStateFlow()

    // Last Detected Pluck feedback
    private val _lastDetectedPluckNote = MutableStateFlow<String?>(null)
    val lastDetectedPluckNote: StateFlow<String?> = _lastDetectedPluckNote.asStateFlow()

    // Mic & Pitch State
    val pitchState: StateFlow<PitchResult> = pitchDetector.pitchState

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // In Tune celebration state
    private val _inTuneSuccess = MutableStateFlow(false)
    val inTuneSuccess: StateFlow<Boolean> = _inTuneSuccess.asStateFlow()

    // Account & Dialog
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> = _showAccountDialog.asStateFlow()

    // History and AI Consultations
    val tuningHistory: StateFlow<List<TuningHistoryEntity>> = repository.tuningHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val guruConsultations: StateFlow<List<GuruConsultationEntity>> = repository.guruConsultations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Guru Chat state
    private val _guruInputText = MutableStateFlow("")
    val guruInputText: StateFlow<String> = _guruInputText.asStateFlow()

    private val _guruAttachedImageUri = MutableStateFlow<Uri?>(null)
    val guruAttachedImageUri: StateFlow<Uri?> = _guruAttachedImageUri.asStateFlow()

    private val _isGuruThinking = MutableStateFlow(false)
    val isGuruThinking: StateFlow<Boolean> = _isGuruThinking.asStateFlow()

    private var lastRecordedTuneTime = 0L

    init {
        // Initialize target frequency state for the default instrument's first string
        _selectedInstrument.value.strings.firstOrNull()?.let {
            pitchDetector.updateTargetFrequency(it.targetFrequencyHz)
        }

        // Collect pitch state to auto-switch string in auto-detect mode & record successes
        viewModelScope.launch {
            pitchDetector.pitchState.collect { pitch ->
                if (!pitch.isSilence && pitch.frequency > 0) {
                    if (_isAutoDetectMode.value) {
                        // Find closest string in currently selected instrument
                        val closest = findClosestString(pitch.frequency, _selectedInstrument.value)
                        if (closest != null) {
                            val swaraText = if (closest.swara.isNotEmpty()) " (${closest.swara})" else ""
                            _lastDetectedPluckNote.value = "${closest.name}$swaraText"
                            if (closest.index != _selectedString.value?.index) {
                                _selectedString.value = closest
                                pitchDetector.updateTargetFrequency(closest.targetFrequencyHz)
                            }
                        }
                    }

                    // Check if In Tune
                    if (pitch.isInTune) {
                        _inTuneSuccess.value = true
                        val now = System.currentTimeMillis()
                        if (now - lastRecordedTuneTime > 3000) {
                            lastRecordedTuneTime = now
                            triggerInTuneHaptics()
                            viewModelScope.launch {
                                ToneGenerator.playInTuneChime()
                            }
                            _selectedString.value?.let { str ->
                                repository.recordTuningSuccess(
                                    instrumentId = _selectedInstrument.value.id,
                                    instrumentName = _selectedInstrument.value.nameEnglish,
                                    stringName = str.name,
                                    targetHz = str.targetFrequencyHz,
                                    detectedHz = pitch.frequency,
                                    centsOffset = pitch.cents
                                )
                            }
                        }
                    } else {
                        _inTuneSuccess.value = false
                    }
                }
            }
        }
    }

    private fun triggerInTuneHaptics() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun setAutoSoundListener(enabled: Boolean) {
        _isAutoSoundListener.value = enabled
        _isAutoDetectMode.value = enabled
        if (enabled) {
            startListening()
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectInstrument(instrument: Instrument) {
        _selectedInstrument.value = instrument
        val firstString = instrument.strings.firstOrNull()
        _selectedString.value = firstString
        firstString?.let {
            pitchDetector.updateTargetFrequency(it.targetFrequencyHz)
        }
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveProfile(current.copy(activeInstrumentId = instrument.id))
        }
    }

    fun selectString(stringItem: InstrumentString) {
        _selectedString.value = stringItem
        pitchDetector.updateTargetFrequency(stringItem.targetFrequencyHz)
        playReferenceTone(stringItem.targetFrequencyHz)
    }

    fun toggleAutoDetectMode() {
        _isAutoDetectMode.value = !_isAutoDetectMode.value
    }

    fun startListening() {
        if (!_isListening.value) {
            pitchDetector.startListening(viewModelScope) {
                _selectedString.value?.targetFrequencyHz
            }
            _isListening.value = true
        }
    }

    fun stopListening() {
        if (_isListening.value) {
            pitchDetector.stopListening()
            _isListening.value = false
        }
    }

    fun toggleListening() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun playReferenceTone(frequencyHz: Float) {
        viewModelScope.launch {
            ToneGenerator.playStringTone(frequencyHz)
        }
    }

    fun setShowAccountDialog(show: Boolean) {
        _showAccountDialog.value = show
    }

    fun saveGoogleProfile(name: String, email: String) {
        viewModelScope.launch {
            val updated = UserProfileEntity(
                userId = "primary_user",
                displayName = name,
                email = email,
                isGoogleConnected = true,
                activeInstrumentId = _selectedInstrument.value.id,
                joinedTimestamp = System.currentTimeMillis()
            )
            repository.saveProfile(updated)
            _showAccountDialog.value = false
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            val guest = UserProfileEntity(
                userId = "primary_user",
                displayName = "Guest Musician",
                email = "guest@stringtuner.app",
                isGoogleConnected = false,
                activeInstrumentId = _selectedInstrument.value.id
            )
            repository.saveProfile(guest)
            _showAccountDialog.value = false
        }
    }

    // Guru Chat Functions
    fun setGuruInputText(text: String) {
        _guruInputText.value = text
    }

    fun setGuruAttachedImage(uri: Uri?) {
        _guruAttachedImageUri.value = uri
    }

    fun askGuru(customQuestion: String? = null) {
        val question = (customQuestion ?: _guruInputText.value).trim()
        if (question.isEmpty()) return

        val imageUri = _guruAttachedImageUri.value
        _isGuruThinking.value = true
        _guruInputText.value = ""
        _guruAttachedImageUri.value = null

        viewModelScope.launch {
            try {
                repository.askGuru(
                    question = question,
                    instrumentName = _selectedInstrument.value.nameHindi,
                    imageUri = imageUri
                )
            } finally {
                _isGuruThinking.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteConsultation(id: Long) {
        viewModelScope.launch {
            repository.deleteConsultation(id)
        }
    }

    private fun findClosestString(detectedHz: Float, instrument: Instrument): InstrumentString? {
        if (instrument.strings.isEmpty()) return null
        return instrument.strings.minByOrNull { str ->
            abs(1200.0 * log2((detectedHz / str.targetFrequencyHz).toDouble()))
        }
    }

    override fun onCleared() {
        super.onCleared()
        pitchDetector.stopListening()
        ToneGenerator.stopTone()
    }
}
