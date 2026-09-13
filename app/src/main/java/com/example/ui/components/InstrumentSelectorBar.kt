package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Instrument
import com.example.data.model.InstrumentCategory
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
import java.util.Locale

/**
 * Interactive instrument selector offering both a dropdown menu and a horizontal scrolling list.
 * Selecting any instrument (Guitar, Veena, Sitar, Violin, etc.) immediately updates the target
 * frequency state for real-time pitch detection logic.
 */
@Composable
fun InstrumentSelectorBar(
    instruments: List<Instrument>,
    selectedInstrument: Instrument,
    selectedString: InstrumentString?,
    onSelectInstrument: (Instrument) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    // Auto-scroll the horizontal list to the selected instrument
    LaunchedEffect(selectedInstrument.id) {
        val selectedIdx = instruments.indexOfFirst { it.id == selectedInstrument.id }
        if (selectedIdx >= 0) {
            lazyListState.animateScrollToItem(selectedIdx)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceElevated, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("instrument_selector_bar")
    ) {
        // Row 1: Header + Dropdown Menu Trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Instrument Selector",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = "SELECT INSTRUMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${selectedInstrument.nameEnglish} (${selectedInstrument.category.titleEnglish})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Dropdown Menu Button & Popup
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("instrument_dropdown_trigger"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Dropdown",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Open instrument dropdown",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkSurfaceElevated, RoundedCornerShape(8.dp))
                        .testTag("instrument_dropdown_menu")
                ) {
                    instruments.forEach { inst ->
                        val isCurrent = inst.id == selectedInstrument.id
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = inst.nameEnglish,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) TunerGreen else TextPrimary
                                        )
                                        Text(
                                            text = "${inst.category.titleEnglish} • ${inst.strings.size} Strings (${inst.tuningName})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Currently selected",
                                            tint = TunerGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectInstrument(inst)
                                isDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_${inst.id}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Horizontal Scrolling List Component
        LazyRow(
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("instrument_horizontal_list")
        ) {
            itemsIndexed(instruments) { _, inst ->
                val isSelected = inst.id == selectedInstrument.id
                val firstString = inst.strings.firstOrNull()

                val cardBg by animateColorAsState(
                    targetValue = if (isSelected) TunerGreen.copy(alpha = 0.14f) else DarkSurfaceCard,
                    animationSpec = tween(150),
                    label = "card_bg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) TunerGreen else DarkSurfaceElevated,
                    animationSpec = tween(150),
                    label = "card_border"
                )

                Surface(
                    onClick = { onSelectInstrument(inst) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("instrument_chip_${inst.id}"),
                    color = cardBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Icon Glyph
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) TunerGreen.copy(alpha = 0.25f)
                                    else DarkSurfaceElevated
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (inst.category) {
                                    InstrumentCategory.INDIAN_CLASSICAL -> Icons.Default.GraphicEq
                                    InstrumentCategory.WESTERN_STRINGS -> Icons.Default.MusicNote
                                    InstrumentCategory.FOLK_ACOUSTIC -> Icons.Default.MusicNote
                                },
                                contentDescription = null,
                                tint = if (isSelected) TunerGreen else GoldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = inst.nameEnglish,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TunerGreen else TextPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = TunerGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            val targetPreview = firstString?.let { str ->
                                "${inst.strings.size} strings • ${str.name} (${String.format(Locale.US, "%.1f", str.targetFrequencyHz)} Hz)"
                            } ?: "${inst.strings.size} strings"

                            Text(
                                text = targetPreview,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: Target Frequency Pitch State Feedback Bar
        val currentTargetHz = selectedString?.targetFrequencyHz
            ?: selectedInstrument.strings.firstOrNull()?.targetFrequencyHz ?: 0f
        val currentStringLabel = selectedString?.let {
            val swaraPart = if (it.swara.isNotEmpty()) " (${it.swara})" else ""
            "${it.name}$swaraPart"
        } ?: selectedInstrument.strings.firstOrNull()?.name ?: "String 1"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBackground.copy(alpha = 0.75f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TunerGreen)
                )
                Text(
                    text = "Pitch Detection Target:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = currentStringLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = String.format(Locale.US, "%.2f Hz", currentTargetHz),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TunerCyan,
                fontSize = 11.sp
            )
        }
    }
}
