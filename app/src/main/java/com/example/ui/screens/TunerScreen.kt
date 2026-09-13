package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.PitchResult
import com.example.data.model.Instrument
import com.example.data.model.InstrumentString
import com.example.ui.MainViewModel
import com.example.ui.components.AutoSoundListenerCard
import com.example.ui.components.GuitarTunaMeter
import com.example.ui.components.HeadstockPegBoard
import com.example.ui.components.InstrumentSelectorBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TunerCyan
import com.example.ui.theme.TunerGreen

@Composable
fun TunerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedInstrument by viewModel.selectedInstrument.collectAsStateWithLifecycle()
    val selectedString by viewModel.selectedString.collectAsStateWithLifecycle()
    val pitchResult by viewModel.pitchState.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isAutoMode by viewModel.isAutoDetectMode.collectAsStateWithLifecycle()
    val isAutoSoundListener by viewModel.isAutoSoundListener.collectAsStateWithLifecycle()
    val lastPluckNote by viewModel.lastDetectedPluckNote.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Proactively launch permission request and start listening seamlessly on screen entry
    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            viewModel.startListening()
        }
    }

    // Auto-start listening if permission already granted
    DisposableEffect(Unit) {
        if (hasMicPermission) {
            viewModel.startListening()
        }

        onDispose {
            viewModel.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Title / Auto Mode Switch & Account Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Precision Tuner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Right Actions: Auto Detect Chip & Google Account Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Auto Detect Toggle Chip
                FilterChip(
                    selected = isAutoMode,
                    onClick = { viewModel.toggleAutoDetectMode() },
                    label = {
                        Text(
                            text = if (isAutoMode) "Auto" else "Manual",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TunerGreen.copy(alpha = 0.2f),
                        selectedLabelColor = TunerGreen,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isAutoMode) TunerGreen else DarkSurfaceElevated,
                        enabled = true,
                        selected = isAutoMode
                    ),
                    modifier = Modifier.testTag("auto_mode_chip")
                )

                // Google Account Icon
                IconButton(
                    onClick = { viewModel.setShowAccountDialog(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .testTag("google_account_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        tint = if (userProfile?.isGoogleConnected == true) TunerGreen else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auto Sound Listener Card (Continuous Acoustic Detection & Pluck Guidance)
        AutoSoundListenerCard(
            isListening = isListening,
            isAutoSoundListener = isAutoSoundListener,
            hasMicPermission = hasMicPermission,
            selectedInstrument = selectedInstrument,
            selectedString = selectedString,
            pitchResult = pitchResult,
            lastPluckNote = lastPluckNote,
            onToggleAutoListener = { enabled ->
                viewModel.setAutoSoundListener(enabled)
            },
            onRequestMicPermission = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Instrument Selector (Dropdown Menu & Horizontal Scrolling List)
        InstrumentSelectorBar(
            instruments = viewModel.allInstruments,
            selectedInstrument = selectedInstrument,
            selectedString = selectedString,
            onSelectInstrument = { inst ->
                viewModel.selectInstrument(inst)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Precision GuitarTuna-Style Arc Meter
        GuitarTunaMeter(
            targetString = selectedString,
            pitchResult = pitchResult,
            isListening = isListening
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Headstock with interactive tuning pegs
        HeadstockPegBoard(
            instrument = selectedInstrument,
            selectedString = selectedString,
            onStringSelected = { str ->
                viewModel.selectString(str)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Controls: Mic Toggle & Reference Tone Play
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Reference Tone Button
            Button(
                onClick = {
                    selectedString?.let { viewModel.playReferenceTone(it.targetFrequencyHz) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("play_ref_tone_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Tone",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Mic Toggle FAB
            FloatingActionButton(
                onClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPerm) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.toggleListening()
                    }
                },
                containerColor = if (isListening) TunerGreen else DarkSurfaceElevated,
                contentColor = if (isListening) DarkBackground else TextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .testTag("mic_toggle_fab")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = if (isListening) "Mute Microphone" else "Enable Microphone",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
