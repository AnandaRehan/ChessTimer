package com.ehan.chesstimer.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehan.chesstimer.data.PlayerSide
import com.ehan.chesstimer.ui.components.CenterControlBar
import com.ehan.chesstimer.ui.components.PlayerClockCard
import com.ehan.chesstimer.ui.components.SettingsDialog
import com.ehan.chesstimer.ui.theme.DarkBackground
import com.ehan.chesstimer.viewmodel.ChessTimerViewModel

@Composable
fun ChessTimerScreen(
    viewModel: ChessTimerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("chess_timer_screen"),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(8.dp)
        ) {
            if (isLandscape) {
                // Landscape Layout: Left (Player 1) vs Right (Player 2)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Player 1 (White)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        PlayerClockCard(
                            playerState = uiState.player1,
                            isActive = uiState.activePlayer == PlayerSide.PLAYER_1,
                            gameStatus = uiState.gameStatus,
                            rotated = false,
                            onTap = { viewModel.onPlayerTap(PlayerSide.PLAYER_1) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Right: Player 2 (Black)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        PlayerClockCard(
                            playerState = uiState.player2,
                            isActive = uiState.activePlayer == PlayerSide.PLAYER_2,
                            gameStatus = uiState.gameStatus,
                            rotated = false,
                            onTap = { viewModel.onPlayerTap(PlayerSide.PLAYER_2) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Floating Center Controls in Landscape (Docked at Bottom or Center)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .width(420.dp)
                ) {
                    CenterControlBar(
                        gameStatus = uiState.gameStatus,
                        timeControl = uiState.timeControl,
                        soundEnabled = uiState.soundEnabled,
                        hapticEnabled = uiState.hapticEnabled,
                        onTogglePlayPause = { viewModel.togglePauseResume() },
                        onReset = { viewModel.resetGame() },
                        onOpenSettings = { viewModel.openCustomDialog() },
                        onToggleSound = { viewModel.toggleSound() },
                        onToggleHaptic = { viewModel.toggleHaptic() }
                    )
                }
            } else {
                // Portrait Layout: Top (Player 2, Rotated 180°) vs Bottom (Player 1)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top: Player 2 (Black) - Rotated 180 degrees for opposite player
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        PlayerClockCard(
                            playerState = uiState.player2,
                            isActive = uiState.activePlayer == PlayerSide.PLAYER_2,
                            gameStatus = uiState.gameStatus,
                            rotated = true,
                            onTap = { viewModel.onPlayerTap(PlayerSide.PLAYER_2) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Bottom: Player 1 (White) - Normal orientation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        PlayerClockCard(
                            playerState = uiState.player1,
                            isActive = uiState.activePlayer == PlayerSide.PLAYER_1,
                            gameStatus = uiState.gameStatus,
                            rotated = false,
                            onTap = { viewModel.onPlayerTap(PlayerSide.PLAYER_1) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Floating Center Controls in Portrait (Dead Center between player 1 and 2)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    CenterControlBar(
                        gameStatus = uiState.gameStatus,
                        timeControl = uiState.timeControl,
                        soundEnabled = uiState.soundEnabled,
                        hapticEnabled = uiState.hapticEnabled,
                        onTogglePlayPause = { viewModel.togglePauseResume() },
                        onReset = { viewModel.resetGame() },
                        onOpenSettings = { viewModel.openCustomDialog() },
                        onToggleSound = { viewModel.toggleSound() },
                        onToggleHaptic = { viewModel.toggleHaptic() }
                    )
                }
            }
        }

        // Settings Bottom Sheet / Modal
        if (uiState.isCustomDialogOpen) {
            SettingsDialog(
                currentTimeControl = uiState.timeControl,
                soundEnabled = uiState.soundEnabled,
                hapticEnabled = uiState.hapticEnabled,
                onSelectTimeControl = { tc -> viewModel.setTimeControl(tc) },
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptic = { viewModel.toggleHaptic() },
                onDismiss = { viewModel.closeCustomDialog() }
            )
        }
    }
}
