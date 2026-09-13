package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Instrument
import com.example.data.model.InstrumentString
import com.example.data.model.PegSide
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StringActive
import com.example.ui.theme.StringWire
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TunerGreen
import java.util.Locale

@Composable
fun HeadstockPegBoard(
    instrument: Instrument,
    selectedString: InstrumentString?,
    onStringSelected: (InstrumentString) -> Unit,
    modifier: Modifier = Modifier
) {
    val leftStrings = instrument.strings.filter { it.pegSide == PegSide.LEFT }
    val rightStrings = instrument.strings.filter { it.pegSide == PegSide.RIGHT }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceCard.copy(alpha = 0.6f))
            .border(1.dp, DarkSurfaceElevated, RoundedCornerShape(20.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp)
    ) {
        // Vertical string wires canvas in the background center
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f

            // Draw stylized instrument neck / headstock silhouette
            drawRoundRect(
                color = DarkBackground.copy(alpha = 0.8f),
                topLeft = Offset(centerX - 42f, 0f),
                size = androidx.compose.ui.geometry.Size(84f, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // Draw string wires
            val totalStrings = instrument.strings.size
            if (totalStrings > 1) {
                val step = 64f / (totalStrings - 1).coerceAtLeast(1)
                instrument.strings.forEachIndexed { i, s ->
                    val stringX = centerX - 32f + (i * step)
                    val isSelected = s.index == selectedString?.index
                    drawLine(
                        color = if (isSelected) StringActive else StringWire.copy(alpha = 0.45f),
                        start = Offset(stringX, 0f),
                        end = Offset(stringX, height),
                        strokeWidth = if (isSelected) 3.5f else 1.8f
                    )
                }
            }
        }

        // Left & Right tuning pegs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Peg Column
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                leftStrings.forEach { stringItem ->
                    PegButton(
                        item = stringItem,
                        isSelected = selectedString?.index == stringItem.index,
                        onSelect = { onStringSelected(stringItem) },
                        isLeft = true
                    )
                }
            }

            // Center Headstock Emblem / Instrument Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = instrument.nameEnglish.split(" ").firstOrNull() ?: instrument.nameEnglish,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    fontSize = 11.sp
                )
                Text(
                    text = "${instrument.strings.size} Strings",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Right Peg Column
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                rightStrings.forEach { stringItem ->
                    PegButton(
                        item = stringItem,
                        isSelected = selectedString?.index == stringItem.index,
                        onSelect = { onStringSelected(stringItem) },
                        isLeft = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PegButton(
    item: InstrumentString,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) TunerGreen else DarkSurfaceElevated,
        label = "peg_border"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) DarkSurfaceElevated else DarkSurfaceCard,
        label = "peg_bg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("peg_${item.index}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!isLeft) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Listen",
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) TunerGreen else TextMuted
            )
        }

        // Circular Note Peg Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) TunerGreen else DarkBackground)
                .border(1.dp, if (isSelected) TunerGreen else TextMuted, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.note,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DarkBackground else TextPrimary,
                fontSize = 13.sp
            )
        }

        // String Name and Frequency
        Column {
            Text(
                text = if (item.swara.isNotEmpty()) "${item.swara} (${item.name})" else item.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = String.format(Locale.US, "%.1f Hz", item.targetFrequencyHz),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) TunerGreen else TextMuted,
                fontSize = 9.sp
            )
        }

        if (isLeft) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Listen",
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) TunerGreen else TextMuted
            )
        }
    }
}
