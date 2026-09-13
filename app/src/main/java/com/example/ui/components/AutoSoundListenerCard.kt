package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PitchResult
import com.example.data.model.Instrument
import com.example.data.model.InstrumentString
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TunerCyan
import com.example.ui.theme.TunerGreen
import com.example.ui.theme.TunerRed
import com.example.ui.theme.TunerAmber
import java.util.Locale
import kotlin.math.abs

/**
 * Auto Sound Listener Card:
 * Provides continuous, hands-free acoustic listening for string plucks.
 * Automatically identifies which string is plucked, locks to the target frequency,
 * displays real-time tuning guidance, and confirms when perfectly in-tune.
 */
@Composable
fun AutoSoundListenerCard(
    isListening: Boolean,
    isAutoSoundListener: Boolean,
    hasMicPermission: Boolean,
    selectedInstrument: Instrument,
    selectedString: InstrumentString?,
    pitchResult: PitchResult,
    lastPluckNote: String?,
    onToggleAutoListener: (Boolean) -> Unit,
    onRequestMicPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val borderTint by animateColorAsState(
        targetValue = when {
            !isListening -> DarkSurfaceElevated
            pitchResult.isInTune -> TunerGreen
            !pitchResult.isSilence && pitchResult.frequency > 0 -> TunerCyan
            else -> GoldAccent.copy(alpha = 0.4f)
        },
        animationSpec = tween(200),
        label = "border_tint"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.2.dp, borderTint, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("auto_sound_listener_card")
    ) {
        // Top Header: Title, Live Status Indicator & Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pulsing Mic Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) TunerGreen.copy(alpha = 0.18f * pulseAlpha)
                            else DarkSurfaceElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Auto Sound Listener Mic",
                        tint = if (isListening) TunerGreen else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Auto Sound Listener",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TunerGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TunerGreen
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isListening) "Listening hands-free • Auto-detects plucks" else "Paused • Tap switch to auto-listen",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isListening) TunerCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Toggle Switch
            Switch(
                checked = isListening && isAutoSoundListener,
                onCheckedChange = { checked ->
                    if (!hasMicPermission && checked) {
                        onRequestMicPermission()
                    } else {
                        onToggleAutoListener(checked)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TunerGreen,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceElevated
                ),
                modifier = Modifier.testTag("auto_listener_switch")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State 1: Mic Permission Prompt Banner (if not granted)
        if (!hasMicPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Microphone Access Required",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "Enable mic permission so the auto sound listener can hear your instrument notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRequestMicPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("grant_mic_permission_btn")
                    ) {
                        Text(
                            text = "Enable",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else if (isListening) {
            // State 2: Active Listening & Live Pluck Detection Feedback
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Visualizer Waveform & Sound Sensitivity Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (pitchResult.isSilence) TextMuted else TunerCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (pitchResult.isSilence) "Waiting for pluck..." else "Pluck detected!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pitchResult.isSilence) TextMuted else TunerCyan,
                            fontSize = 11.sp
                        )
                    }

                    // Audio Sensitivity Meter (dynamic bars reacting to amplitude)
                    val normalizedAmp = (pitchResult.amplitude * 40f).coerceIn(0.05f, 1.0f)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.height(14.dp)
                    ) {
                        for (bar in 1..5) {
                            val barHeightFactor = (normalizedAmp * (bar * 0.25f)).coerceIn(0.2f, 1.0f)
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height((14 * barHeightFactor).dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (pitchResult.isInTune) TunerGreen
                                        else if (!pitchResult.isSilence) TunerCyan
                                        else DarkSurfaceElevated
                                    )
                            )
                        }
                    }
                }

                // Pluck Auto-Lock & Tuning Guidance Pill
                val currentString = selectedString
                if (pitchResult.frequency > 0 && !pitchResult.isSilence && currentString != null) {
                    val diff = pitchResult.frequency - currentString.targetFrequencyHz
                    val cents = pitchResult.cents

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    pitchResult.isInTune -> TunerGreen.copy(alpha = 0.22f)
                                    cents < -3.5f -> TunerAmber.copy(alpha = 0.16f)
                                    else -> TunerRed.copy(alpha = 0.16f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    pitchResult.isInTune -> TunerGreen
                                    cents < -3.5f -> TunerAmber
                                    else -> TunerRed
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (pitchResult.isInTune) Icons.Default.CheckCircle else Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (pitchResult.isInTune) TunerGreen else if (cents < -3.5f) TunerAmber else TunerRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when {
                                    pitchResult.isInTune -> "IN TUNE! (Locked)"
                                    cents < -3.5f -> "TIGHTEN PEG (Too Low)"
                                    else -> "LOOSEN PEG (Too High)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pitchResult.isInTune) TunerGreen else if (cents < -3.5f) TunerAmber else TunerRed,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = String.format(Locale.US, "%+.1f cents", cents),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    // Idle guidance
                    Text(
                        text = "Tip: Pluck any string on your ${selectedInstrument.nameEnglish}. The auto listener automatically locks to the matching string note.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}
