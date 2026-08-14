package com.ehan.chesstimer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ehan.chesstimer.data.GameStatus
import com.ehan.chesstimer.data.PlayerSide
import com.ehan.chesstimer.data.PlayerState
import com.ehan.chesstimer.ui.theme.ActiveAccentBadge
import com.ehan.chesstimer.ui.theme.ActiveCardBg
import com.ehan.chesstimer.ui.theme.ActiveCardText
import com.ehan.chesstimer.ui.theme.ActiveCardTextSecondary
import com.ehan.chesstimer.ui.theme.DangerRed
import com.ehan.chesstimer.ui.theme.DangerRedBg
import com.ehan.chesstimer.ui.theme.FlaggedRed
import com.ehan.chesstimer.ui.theme.InactiveCardBg
import com.ehan.chesstimer.ui.theme.InactiveCardText
import com.ehan.chesstimer.ui.theme.InactiveCardTextSecondary
import com.ehan.chesstimer.ui.theme.StatusActiveRed
import com.ehan.chesstimer.ui.theme.StatusWaitEmerald
import com.ehan.chesstimer.ui.theme.SurfaceCardActive
import com.ehan.chesstimer.ui.theme.WarningAmber

@Composable
fun PlayerClockCard(
    playerState: PlayerState,
    isActive: Boolean,
    gameStatus: GameStatus,
    rotated: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = gameStatus == GameStatus.RUNNING
    val isLowTime = isRunning && isActive && playerState.timeRemainingMillis in 1..10_000L
    val isFlagged = playerState.isFlagged

    // Low time pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "low_time_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Dynamic background color matching Elegant Dark
    val targetBgColor = when {
        isFlagged -> FlaggedRed
        isLowTime -> DangerRedBg
        isActive && isRunning -> ActiveCardBg
        else -> InactiveCardBg
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 180),
        label = "bg_color"
    )

    val contentColor = when {
        isFlagged -> Color.White
        isLowTime -> Color.White
        isActive && isRunning -> ActiveCardText
        else -> InactiveCardText
    }

    val subContentColor = when {
        isFlagged -> Color.White.copy(alpha = 0.8f)
        isLowTime -> Color.White.copy(alpha = 0.8f)
        isActive && isRunning -> ActiveCardTextSecondary
        else -> InactiveCardTextSecondary
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(if (playerState.side == PlayerSide.PLAYER_1) "player_1_clock" else "player_2_clock")
            .clip(RoundedCornerShape(32.dp))
            .background(animatedBgColor)
            .border(
                width = if (isActive && isRunning) 2.dp else 1.dp,
                color = when {
                    isFlagged -> FlaggedRed
                    isLowTime -> WarningAmber
                    isActive && isRunning -> ActiveCardText.copy(alpha = 0.3f)
                    else -> Color.White.copy(alpha = 0.08f)
                },
                shape = RoundedCornerShape(32.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = if (isActive && isRunning) ActiveCardText else Color.White
                ),
                onClick = onTap
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (rotated) {
                        rotationZ = 180f
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Player Title with dot badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (playerState.side == PlayerSide.PLAYER_1) {
                                    if (isActive && isRunning) ActiveCardText else Color.White
                                } else {
                                    if (isActive && isRunning) ActiveCardText.copy(alpha = 0.4f) else Color(0xFF1E293B)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive && isRunning) ActiveCardText.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (playerState.side == PlayerSide.PLAYER_1) "PLAYER ONE (WHITE)" else "PLAYER TWO (BLACK)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = subContentColor
                        )
                    )
                }

                // Move Counter Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActive && isRunning) {
                        ActiveCardText.copy(alpha = 0.12f)
                    } else {
                        Color.Black.copy(alpha = 0.25f)
                    }
                ) {
                    Text(
                        text = "Move #${playerState.moveCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Center Display: Monospace Large Digital Clock & Stats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isFlagged) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Time out",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TIME OUT!",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = "Waktu Habis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                } else {
                    val formattedTime = formatChessTime(playerState.timeRemainingMillis)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(if (isLowTime) pulseAlpha else 1f)
                    ) {
                        Text(
                            text = formattedTime.mainTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (formattedTime.fraction.isNotEmpty()) 60.sp else 74.sp,
                                letterSpacing = (-2).sp,
                                color = when {
                                    isLowTime -> WarningAmber
                                    else -> contentColor
                                }
                            ),
                            textAlign = TextAlign.Center
                        )

                        if (formattedTime.fraction.isNotEmpty()) {
                            Text(
                                text = formattedTime.fraction,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowTime) WarningAmber else contentColor.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Row: Moves & Status with vertical divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        // Moves stat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MOVES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = subContentColor.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${playerState.moveCount}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(
                                    if (isActive && isRunning) {
                                        ActiveCardText.copy(alpha = 0.15f)
                                    } else {
                                        Color.White.copy(alpha = 0.15f)
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        // Status stat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = subContentColor.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            val statusText = when {
                                gameStatus == GameStatus.NOT_STARTED -> "Ready"
                                gameStatus == GameStatus.PAUSED -> "Paused"
                                isActive && isRunning -> "Active"
                                else -> "Wait"
                            }

                            val statusColor = when {
                                gameStatus == GameStatus.NOT_STARTED -> subContentColor
                                gameStatus == GameStatus.PAUSED -> WarningAmber
                                isActive && isRunning -> StatusActiveRed
                                else -> StatusWaitEmerald
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Prompt / Tap status indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isFlagged -> {
                        Text(
                            text = "Game Ended",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                    gameStatus == GameStatus.NOT_STARTED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Tap to start",
                                tint = subContentColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TAP TO START",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = subContentColor.copy(alpha = 0.85f),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                    isActive && isRunning -> {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ActiveCardText.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "TAP AFTER MOVE ➔",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ActiveCardText,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                    gameStatus == GameStatus.PAUSED -> {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = subContentColor.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                    else -> {
                        Text(
                            text = "Opponent's Turn",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = subContentColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}

data class FormattedChessTime(
    val mainTime: String,
    val fraction: String = ""
)

fun formatChessTime(timeMillis: Long): FormattedChessTime {
    val totalSeconds = (timeMillis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    val tenths = ((timeMillis % 1000L) / 100L).coerceAtLeast(0L)

    return when {
        hours > 0 -> {
            val hStr = hours.toString()
            val mStr = minutes.toString().padStart(2, '0')
            val sStr = seconds.toString().padStart(2, '0')
            FormattedChessTime("$hStr:$mStr:$sStr")
        }
        totalSeconds < 20 -> {
            val sStr = seconds.toString().padStart(2, '0')
            FormattedChessTime("00:$sStr", ".$tenths")
        }
        else -> {
            val mStr = minutes.toString().padStart(2, '0')
            val sStr = seconds.toString().padStart(2, '0')
            FormattedChessTime("$mStr:$sStr")
        }
    }
}

