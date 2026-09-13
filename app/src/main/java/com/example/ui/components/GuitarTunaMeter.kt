package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PitchResult
import com.example.data.model.InstrumentString
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TunerAmber
import com.example.ui.theme.TunerCyan
import com.example.ui.theme.TunerGreen
import com.example.ui.theme.TunerRed
import java.util.Locale
import kotlin.math.abs

@Composable
fun GuitarTunaMeter(
    targetString: InstrumentString?,
    pitchResult: PitchResult,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedCents by animateFloatAsState(
        targetValue = if (pitchResult.isSilence) 0f else pitchResult.cents,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "cents_anim"
    )

    val animatedDiffHz by animateFloatAsState(
        targetValue = if (pitchResult.isSilence) 0f else pitchResult.frequencyDifference,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "diff_hz_anim"
    )

    val isInTune = !pitchResult.isSilence && pitchResult.isInTune
    val meterColor by animateColorAsState(
        targetValue = when {
            pitchResult.isSilence -> TextMuted
            isInTune -> TunerGreen
            animatedCents < -3.5f -> TunerAmber // Flat (need to tune up)
            else -> TunerRed // Sharp (need to tune down)
        },
        animationSpec = tween(durationMillis = 200),
        label = "meter_color"
    )

    val targetHz = targetString?.targetFrequencyHz ?: pitchResult.targetFrequency

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurfaceCard)
            .border(
                width = if (isInTune) 2.dp else 1.dp,
                color = if (isInTune) TunerGreen.copy(alpha = 0.85f) else DarkSurfaceElevated,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 18.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Status Bar: Mic status and Tuning Advice
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isListening) TunerGreen else TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isListening) "Live Microphone" else "Mic Paused",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isListening) TunerGreen else TextSecondary
                )
            }

            // Real-time Advice
            val adviceText = when {
                !isListening -> "Turn on mic to tune"
                pitchResult.isSilence -> "Pluck a string"
                isInTune -> "✓ IN TUNE"
                animatedDiffHz < -0.3f -> "▲ Tune Up ♯"
                else -> "▼ Tune Down ♭"
            }

            Text(
                text = adviceText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isInTune) FontWeight.Bold else FontWeight.SemiBold,
                color = meterColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Note Badge (GuitarTuna Style)
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(
                    elevation = if (isInTune) 20.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = if (isInTune) TunerGreen else Color.Transparent
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DarkSurfaceElevated,
                            DarkSurfaceCard
                        )
                    )
                )
                .border(
                    width = if (isInTune) 3.5.dp else 2.dp,
                    color = meterColor,
                    shape = CircleShape
                )
                .testTag("note_badge"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Note Letter
                Text(
                    text = targetString?.note ?: (if (!pitchResult.isSilence) pitchResult.note else "--"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isInTune) TunerGreen else TextPrimary,
                    fontSize = 32.sp
                )

                // Swara / String Identifier
                val subText = targetString?.let {
                    if (it.swara.isNotEmpty()) "${it.swara} • ${it.name}" else it.name
                } ?: "Pitch"

                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Precision Arc / Scale Meter (-50 to +50 cents)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Baseline guide line
            drawLine(
                color = DarkSurfaceElevated,
                start = Offset(20f, centerY),
                end = Offset(width - 20f, centerY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Tick marks (-50 to +50 cents)
            val numTicks = 20
            val rangeWidth = width - 80f
            val tickStep = rangeWidth / numTicks

            for (i in 0..numTicks) {
                val tickX = 40f + (i * tickStep)
                val centVal = -50 + (i * 5)
                val isCenter = centVal == 0
                val isMajor = centVal % 25 == 0

                val tickHeight = when {
                    isCenter -> 24f
                    isMajor -> 16f
                    else -> 10f
                }

                val tickColor = when {
                    isCenter -> if (isInTune) TunerGreen else TunerCyan
                    isMajor -> TextSecondary
                    else -> TextMuted.copy(alpha = 0.5f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(tickX, centerY - tickHeight / 2f),
                    end = Offset(tickX, centerY + tickHeight / 2f),
                    strokeWidth = if (isCenter) 4f else if (isMajor) 2f else 1.5f,
                    cap = StrokeCap.Round
                )
            }

            // Needle position based on animated cents (-50 to +50)
            val clampedCents = animatedCents.coerceIn(-50f, 50f)
            val needleFraction = (clampedCents + 50f) / 100f
            val needleX = 40f + (needleFraction * rangeWidth)

            // Needle Glow and Line
            drawLine(
                color = meterColor,
                start = Offset(needleX, 2f),
                end = Offset(needleX, height - 2f),
                strokeWidth = if (isInTune) 6f else 4f,
                cap = StrokeCap.Round
            )

            // Center target diamond/dot
            drawCircle(
                color = if (isInTune) TunerGreen else TextMuted,
                radius = 4f,
                center = Offset(centerX, centerY)
            )
        }

        // Cents readout banner (-50 to +50)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "♭ Flat (-50¢)",
                style = MaterialTheme.typography.bodySmall,
                color = if (animatedCents < -3.5f && !pitchResult.isSilence) TunerAmber else TextMuted
            )

            val centsFormatted = if (pitchResult.isSilence) "0.0 cents" else String.format(Locale.US, "%+.1f cents", animatedCents)
            Text(
                text = centsFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = meterColor
            )

            Text(
                text = "(+50¢) Sharp ♯",
                style = MaterialTheme.typography.bodySmall,
                color = if (animatedCents > 3.5f && !pitchResult.isSilence) TunerRed else TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // REAL-TIME FREQUENCY DIFFERENCE PANEL (PROMINENT HIGHLIGHT)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceElevated)
                .border(
                    width = 1.dp,
                    color = if (isInTune) TunerGreen.copy(alpha = 0.5f) else DarkSurfaceCard,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("frequency_difference_panel")
        ) {
            Column {
                // Header of Difference Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Frequency Difference",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Real-Time Frequency Difference",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Live Difference Badge
                    val diffSign = if (animatedDiffHz > 0) "+" else ""
                    val diffBadgeText = if (pitchResult.isSilence || targetHz <= 0f) {
                        "Δf: 0.0 Hz"
                    } else {
                        String.format(Locale.US, "Δf: %s%.2f Hz", diffSign, animatedDiffHz)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    pitchResult.isSilence -> DarkSurfaceCard
                                    isInTune -> TunerGreen.copy(alpha = 0.2f)
                                    animatedDiffHz < 0f -> TunerAmber.copy(alpha = 0.2f)
                                    else -> TunerRed.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("frequency_difference_badge")
                    ) {
                        Text(
                            text = diffBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = meterColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3-Column Telemetry: Detected Hz | Difference (Δf) | Target Hz
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Detected
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Detected Pitch",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = if (!pitchResult.isSilence && pitchResult.frequency > 0) {
                                String.format(Locale.US, "%.1f Hz", pitchResult.frequency)
                            } else {
                                "-- Hz"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Column 2: Frequency Difference
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Difference (Δf)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        val diffHzFormatted = if (pitchResult.isSilence || targetHz <= 0f) {
                            "0.00 Hz"
                        } else {
                            val sign = if (animatedDiffHz > 0) "+" else ""
                            String.format(Locale.US, "%s%.2f Hz", sign, animatedDiffHz)
                        }
                        Text(
                            text = diffHzFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = meterColor
                        )
                    }

                    // Column 3: Target String Frequency
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target Note",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = if (targetHz > 0) {
                                String.format(Locale.US, "%.1f Hz", targetHz)
                            } else {
                                "-- Hz"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TunerCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time Frequency Difference Deviation Bar (-10 Hz to +10 Hz range)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                ) {
                    val barWidth = size.width
                    val barHeight = size.height
                    val barCenter = barWidth / 2f

                    // Background track
                    drawRoundRect(
                        color = DarkSurfaceCard,
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Target zone (±0.5 Hz center tolerance)
                    val toleranceWidth = (barWidth / 20f) * 1.0f
                    drawRect(
                        color = TunerGreen.copy(alpha = 0.25f),
                        topLeft = Offset(barCenter - toleranceWidth / 2f, 0f),
                        size = Size(toleranceWidth, barHeight)
                    )

                    // Center reference tick
                    drawLine(
                        color = if (isInTune) TunerGreen else TextMuted,
                        start = Offset(barCenter, 0f),
                        end = Offset(barCenter, barHeight),
                        strokeWidth = 2.5f
                    )

                    // Difference Pointer (clamped between -10 Hz and +10 Hz)
                    if (!pitchResult.isSilence && targetHz > 0f) {
                        val clampedDiffHz = animatedDiffHz.coerceIn(-10f, 10f)
                        val diffFraction = (clampedDiffHz + 10f) / 20f
                        val pointerX = diffFraction * barWidth

                        drawCircle(
                            color = meterColor,
                            radius = 6f,
                            center = Offset(pointerX, barHeight / 2f)
                        )
                    }
                }

                // Bar Range Legend (-10 Hz ... 0 Hz ... +10 Hz)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("-10 Hz (Flat)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                    Text("0 Hz (In Tune)", style = MaterialTheme.typography.labelSmall, color = if (isInTune) TunerGreen else TextMuted, fontSize = 10.sp)
                    Text("+10 Hz (Sharp)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
