package com.ehan.chesstimer.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ehan.chesstimer.data.GameStatus
import com.ehan.chesstimer.data.TimeControl
import com.ehan.chesstimer.ui.theme.AccentDeepPurple
import com.ehan.chesstimer.ui.theme.AccentLavender
import com.ehan.chesstimer.ui.theme.ControlButtonBg
import com.ehan.chesstimer.ui.theme.ControlIconDim
import com.ehan.chesstimer.ui.theme.ControlIconTint
import com.ehan.chesstimer.ui.theme.DangerRed
import com.ehan.chesstimer.ui.theme.DarkBackground
import com.ehan.chesstimer.ui.theme.InactiveCardText
import com.ehan.chesstimer.ui.theme.SurfaceDark

@Composable
fun CenterControlBar(
    gameStatus: GameStatus,
    timeControl: TimeControl,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onTogglePlayPause: () -> Unit,
    onReset: () -> Unit,
    onOpenSettings: () -> Unit,
    onRandomize: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "Reset Pertandingan?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Apakah Anda ingin mengulang timer ke waktu awal (${timeControl.formattedDescription})?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showResetConfirmDialog = false
                        onReset()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DangerRed,
                        contentColor = DarkBackground
                    ),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmDialog = false }
                ) {
                    Text("Batal", color = InactiveCardText)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = InactiveCardText,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Surface(
        modifier = modifier
            .testTag("center_control_bar")
            .clip(RoundedCornerShape(32.dp))
            .background(DarkBackground.copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp)),
        color = DarkBackground.copy(alpha = 0.95f),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Settings & Shuffle / Randomize
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings Square Button
                FilledTonalIconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("settings_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ControlButtonBg,
                        contentColor = ControlIconTint
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Shuffle / Randomize Button
                FilledTonalIconButton(
                    onClick = onRandomize,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("shuffle_sides_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (gameStatus == GameStatus.NOT_STARTED) AccentLavender.copy(alpha = 0.18f) else ControlButtonBg,
                        contentColor = if (gameStatus == GameStatus.NOT_STARTED) AccentLavender else ControlIconTint
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Acak Sisi Pemain",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Time Control Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ControlButtonBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenSettings)
                        .testTag("time_control_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AccentLavender)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = timeControl.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ControlIconTint,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Center Group: Play / Pause Pill Button & Status Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(width = 68.dp, height = 44.dp)
                        .testTag("play_pause_button"),
                    shape = RoundedCornerShape(50),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AccentLavender,
                        contentColor = AccentDeepPurple
                    )
                ) {
                    Icon(
                        imageVector = if (gameStatus == GameStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (gameStatus == GameStatus.RUNNING) "Pause Game" else "Start / Resume Game",
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val statusLabel = when (gameStatus) {
                    GameStatus.RUNNING -> "RUNNING"
                    GameStatus.PAUSED -> "PAUSED"
                    GameStatus.GAME_OVER -> "ENDED"
                    GameStatus.NOT_STARTED -> "READY"
                }

                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                )
            }

            // Right Group: Sound, Haptic & Reset Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Toggle
                FilledTonalIconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sound_toggle_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ControlButtonBg,
                        contentColor = if (soundEnabled) ControlIconTint else ControlIconDim
                    )
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = if (soundEnabled) "Sound On" else "Sound Off",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Haptic Toggle
                FilledTonalIconButton(
                    onClick = onToggleHaptic,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("haptic_toggle_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ControlButtonBg,
                        contentColor = if (hapticEnabled) ControlIconTint else ControlIconDim
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = if (hapticEnabled) "Vibration On" else "Vibration Off",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Reset Button
                FilledTonalIconButton(
                    onClick = {
                        if (gameStatus == GameStatus.RUNNING || gameStatus == GameStatus.PAUSED) {
                            showResetConfirmDialog = true
                        } else {
                            onReset()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("reset_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ControlButtonBg,
                        contentColor = ControlIconTint
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Game",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

