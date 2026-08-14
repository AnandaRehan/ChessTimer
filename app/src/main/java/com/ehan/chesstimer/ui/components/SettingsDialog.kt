package com.ehan.chesstimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ehan.chesstimer.data.TimeCategory
import com.ehan.chesstimer.data.TimeControl
import com.ehan.chesstimer.ui.theme.AccentDeepPurple
import com.ehan.chesstimer.ui.theme.AccentGold
import com.ehan.chesstimer.ui.theme.AccentLavender
import com.ehan.chesstimer.ui.theme.DarkBackground
import com.ehan.chesstimer.ui.theme.InactiveCardText
import com.ehan.chesstimer.ui.theme.InactiveCardTextSecondary
import com.ehan.chesstimer.ui.theme.SurfaceCard
import com.ehan.chesstimer.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentTimeControl: TimeControl,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onSelectTimeControl: (TimeControl) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryIndex by remember {
        val initialCat = currentTimeControl.category
        val index = TimeCategory.values().indexOf(initialCat).coerceAtLeast(0)
        mutableIntStateOf(if (index == -1) 1 else index)
    }

    // Custom time builder state
    var customMinutes by remember { mutableIntStateOf(currentTimeControl.initialMinutes.coerceIn(1, 180)) }
    var customSeconds by remember { mutableIntStateOf(currentTimeControl.initialSeconds.coerceIn(0, 59)) }
    var customIncrement by remember { mutableIntStateOf(currentTimeControl.incrementSeconds.coerceIn(0, 60)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        contentColor = Color.White,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = AccentLavender,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pengaturan Jam Catur",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Categories Tab Row
            val categories = TimeCategory.values()
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = Color.Transparent,
                contentColor = AccentLavender,
                edgePadding = 0.dp,
                divider = {}
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategoryIndex == index) AccentLavender else InactiveCardTextSecondary
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentCategory = categories[selectedCategoryIndex]

            if (currentCategory == TimeCategory.CUSTOM) {
                // Custom Builder Mode
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Kustom Kontrol Waktu",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentLavender
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Minute Selector
                        TimeCounterRow(
                            label = "Waktu Dasar (Menit)",
                            value = customMinutes,
                            unit = "menit",
                            minValue = 0,
                            maxValue = 180,
                            onValueChange = { customMinutes = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Second Selector
                        TimeCounterRow(
                            label = "Waktu Dasar (Detik)",
                            value = customSeconds,
                            unit = "detik",
                            minValue = 0,
                            maxValue = 59,
                            onValueChange = { customSeconds = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Increment Selector
                        TimeCounterRow(
                            label = "Bonus Increment (+Detik/Langkah)",
                            value = customIncrement,
                            unit = "detik",
                            minValue = 0,
                            maxValue = 60,
                            onValueChange = { customIncrement = it }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val totalMillis = (customMinutes * 60_000L) + (customSeconds * 1000L)
                        val isValid = totalMillis > 0L

                        FilledTonalButton(
                            onClick = {
                                if (isValid) {
                                    val name = if (customIncrement > 0) {
                                        "Custom ${customMinutes}m + ${customIncrement}s"
                                    } else {
                                        "Custom ${customMinutes}m ${customSeconds}s"
                                    }
                                    onSelectTimeControl(
                                        TimeControl(
                                            name = name,
                                            initialTimeMillis = totalMillis,
                                            incrementMillis = customIncrement * 1000L,
                                            category = TimeCategory.CUSTOM
                                        )
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = isValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("apply_custom_time_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AccentLavender,
                                contentColor = AccentDeepPurple
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Terapkan Waktu Kustom",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                // Preset List for the selected category
                val presets = TimeControl.PRESETS.filter { it.category == currentCategory }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = currentTimeControl.name == preset.name
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) AccentLavender else SurfaceDark,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentLavender else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    onSelectTimeControl(preset)
                                    onDismiss()
                                }
                                .testTag("preset_${preset.name.replace(" ", "_").lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AccentDeepPurple else Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (preset.incrementMillis > 0) {
                                        "+${preset.incrementSeconds}s increment"
                                    } else {
                                        "No increment"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) AccentDeepPurple.copy(alpha = 0.8f) else InactiveCardTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sound and Vibration Settings Section
            Text(
                text = "Pengaturan Suara & Getaran",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Sound Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = AccentLavender,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Efek Suara (Sound Click)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Bunyi saat pergantian giliran dan peringatan waktu",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = InactiveCardTextSecondary
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { onToggleSound() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentDeepPurple,
                                checkedTrackColor = AccentLavender
                            ),
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Haptic Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = AccentLavender,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Getaran Taktil (Haptics)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Getaran responsif saat menekan jam catur",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = InactiveCardTextSecondary
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { onToggleHaptic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentDeepPurple,
                                checkedTrackColor = AccentLavender
                            ),
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeCounterRow(
    label: String,
    value: Int,
    unit: String,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AccentLavender
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = { if (value > minValue) onValueChange(value - 1) },
                enabled = value > minValue,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = SurfaceCard,
                    contentColor = Color.White
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalIconButton(
                onClick = { if (value < maxValue) onValueChange(value + 1) },
                enabled = value < maxValue,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = SurfaceCard,
                    contentColor = Color.White
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
            }
        }
    }
}
