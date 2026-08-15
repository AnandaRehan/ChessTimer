package com.ehan.chesstimer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ehan.chesstimer.data.ChessColor
import com.ehan.chesstimer.ui.theme.AccentDeepPurple
import com.ehan.chesstimer.ui.theme.AccentLavender
import com.ehan.chesstimer.ui.theme.ControlButtonBg
import com.ehan.chesstimer.ui.theme.ControlIconTint
import com.ehan.chesstimer.ui.theme.DarkBackground
import com.ehan.chesstimer.ui.theme.InactiveCardText
import com.ehan.chesstimer.ui.theme.SurfaceDark

@Composable
fun ShuffleDialog(
    isShuffling: Boolean,
    displayColorP1: ChessColor,
    resultSummary: String?,
    onReShuffle: () -> Unit,
    onManualSwap: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayColorP2 = displayColorP1.opponent

    // Infinite rotation & pulsing animation while shuffling
    val infiniteTransition = rememberInfiniteTransition(label = "shuffle_transition")
    val spinningAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinning_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val finalAngle by animateFloatAsState(
        targetValue = if (displayColorP1 == ChessColor.WHITE) 0f else 180f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "final_coin_angle"
    )

    val currentCoinRotation = if (isShuffling) spinningAngle else finalAngle

    Dialog(
        onDismissRequest = {
            if (!isShuffling) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isShuffling,
            dismissOnClickOutside = !isShuffling,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("shuffle_dialog")
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceDark,
                            DarkBackground
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AccentLavender.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Bar: Title and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AccentLavender.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Randomize",
                                    tint = AccentLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Acak Sisi Pemain",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    if (!isShuffling) {
                        FilledTonalIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = ControlButtonBg,
                                contentColor = InactiveCardText
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center Animated 3D Chess Piece Roulette Coin
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(if (isShuffling) pulseScale else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer decorative glowing ring
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AccentLavender.copy(alpha = if (isShuffling) 0.35f else 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 3D Flipping Piece Coin
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer {
                                rotationY = currentCoinRotation
                                cameraDistance = 12f * density
                            }
                            .clip(CircleShape)
                            .background(
                                if (displayColorP1 == ChessColor.WHITE) Color.White else Color(0xFF1E293B)
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        AccentLavender,
                                        Color.White,
                                        AccentLavender
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (displayColorP1 == ChessColor.WHITE) "♔" else "♚",
                            fontSize = 54.sp,
                            color = if (displayColorP1 == ChessColor.WHITE) Color(0xFF1E293B) else Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Text
                if (isShuffling) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AccentLavender,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mengacak warna & posisi...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AccentLavender
                            )
                        )
                    }
                } else {
                    Text(
                        text = "SISI TERPILIH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = AccentLavender
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Player Slot Cards (Top vs Bottom preview)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Player 2 (Top / Atas)
                    PlayerSideResultCard(
                        playerLabel = "Pemain 2 (Atas)",
                        color = displayColorP2,
                        isFirstMove = displayColorP2 == ChessColor.WHITE,
                        modifier = Modifier.weight(1f)
                    )

                    // Player 1 (Bottom / Bawah)
                    PlayerSideResultCard(
                        playerLabel = "Pemain 1 (Bawah)",
                        color = displayColorP1,
                        isFirstMove = displayColorP1 == ChessColor.WHITE,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Summary Note
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ControlButtonBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isShuffling) {
                                "Putih selalu melangkah pertama pada catur."
                            } else {
                                resultSummary ?: "Putih selalu melangkah pertama!"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (!isShuffling) Color.White else InactiveCardText,
                                fontWeight = if (!isShuffling) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Re-shuffle button
                    OutlinedButton(
                        onClick = onReShuffle,
                        enabled = !isShuffling,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("reshuffle_button"),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(AccentLavender.copy(alpha = 0.5f), Color.White.copy(alpha = 0.2f))
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Acak Lagi",
                            tint = AccentLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Acak Lagi",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentLavender
                            )
                        )
                    }

                    // Swap button
                    FilledTonalButton(
                        onClick = onManualSwap,
                        enabled = !isShuffling,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ControlButtonBg,
                            contentColor = ControlIconTint
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("manual_swap_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Tukar",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Confirm / Start Button
                    FilledTonalButton(
                        onClick = onDismiss,
                        enabled = !isShuffling,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentLavender,
                            contentColor = AccentDeepPurple
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Siap Main",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Siap Main",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSideResultCard(
    playerLabel: String,
    color: ChessColor,
    isFirstMove: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFirstMove) AccentLavender.copy(alpha = 0.12f) else ControlButtonBg
        ),
        border = if (isFirstMove) {
            androidx.compose.foundation.BorderStroke(1.5.dp, AccentLavender.copy(alpha = 0.7f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InactiveCardText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Color circle and piece
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (color == ChessColor.WHITE) Color.White else Color(0xFF1E293B))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = color.symbol,
                        fontSize = 14.sp,
                        color = if (color == ChessColor.WHITE) Color(0xFF1E293B) else Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = color.label.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isFirstMove) AccentLavender else Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // First move badge
            if (isFirstMove) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = AccentLavender
                ) {
                    Text(
                        text = "1ST MOVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            color = AccentDeepPurple,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "Melangkah Ke-2",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = InactiveCardText,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
