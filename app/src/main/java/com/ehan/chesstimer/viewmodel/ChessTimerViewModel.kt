package com.ehan.chesstimer.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehan.chesstimer.audio.AudioHapticHelper
import com.ehan.chesstimer.data.GameStatus
import com.ehan.chesstimer.data.PlayerSide
import com.ehan.chesstimer.data.PlayerState
import com.ehan.chesstimer.data.TimeControl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ChessUiState(
    val timeControl: TimeControl = TimeControl.DEFAULT,
    val player1: PlayerState = PlayerState(
        side = PlayerSide.PLAYER_1,
        name = "Player 1",
        timeRemainingMillis = TimeControl.DEFAULT.initialTimeMillis
    ),
    val player2: PlayerState = PlayerState(
        side = PlayerSide.PLAYER_2,
        name = "Player 2",
        timeRemainingMillis = TimeControl.DEFAULT.initialTimeMillis
    ),
    val activePlayer: PlayerSide? = null,
    val gameStatus: GameStatus = GameStatus.NOT_STARTED,
    val winner: PlayerSide? = null,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val showTenthsUnderSeconds: Int = 20, // show tenths when < 20s
    val isCustomDialogOpen: Boolean = false
)

class ChessTimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var lastTickTimestamp: Long = 0L
    private var audioHapticHelper: AudioHapticHelper? = null

    fun setAudioHapticHelper(helper: AudioHapticHelper) {
        this.audioHapticHelper = helper
    }

    /**
     * Handles tap on a player zone.
     * If the game has not started, tapping Player 1 starts Player 2's timer, or vice versa.
     * If game is running, only tapping the active player zone completes that player's turn, adds increment, and starts opponent's clock.
     */
    fun onPlayerTap(tappedSide: PlayerSide) {
        val state = _uiState.value

        when (state.gameStatus) {
            GameStatus.NOT_STARTED -> {
                // First move
                audioHapticHelper?.triggerTapHaptic(state.hapticEnabled)
                audioHapticHelper?.playClickSound(state.soundEnabled)
                
                // If player 1 taps, player 2's turn begins (or if player 2 taps, player 1 begins)
                val nextActive = if (tappedSide == PlayerSide.PLAYER_1) PlayerSide.PLAYER_2 else PlayerSide.PLAYER_1
                
                _uiState.update { current ->
                    current.copy(
                        gameStatus = GameStatus.RUNNING,
                        activePlayer = nextActive,
                        player1 = if (tappedSide == PlayerSide.PLAYER_1) current.player1.copy(moveCount = 1) else current.player1,
                        player2 = if (tappedSide == PlayerSide.PLAYER_2) current.player2.copy(moveCount = 1) else current.player2
                    )
                }
                startTimerLoop()
            }

            GameStatus.RUNNING -> {
                // Can only tap if it is currently this player's active turn
                if (state.activePlayer == tappedSide) {
                    audioHapticHelper?.triggerTapHaptic(state.hapticEnabled)
                    audioHapticHelper?.playClickSound(state.soundEnabled)

                    val nextActive = if (tappedSide == PlayerSide.PLAYER_1) PlayerSide.PLAYER_2 else PlayerSide.PLAYER_1
                    val increment = state.timeControl.incrementMillis

                    _uiState.update { current ->
                        val updatedP1 = if (tappedSide == PlayerSide.PLAYER_1) {
                            current.player1.copy(
                                timeRemainingMillis = current.player1.timeRemainingMillis + increment,
                                moveCount = current.player1.moveCount + 1
                            )
                        } else {
                            current.player1
                        }

                        val updatedP2 = if (tappedSide == PlayerSide.PLAYER_2) {
                            current.player2.copy(
                                timeRemainingMillis = current.player2.timeRemainingMillis + increment,
                                moveCount = current.player2.moveCount + 1
                            )
                        } else {
                            current.player2
                        }

                        current.copy(
                            activePlayer = nextActive,
                            player1 = updatedP1,
                            player2 = updatedP2
                        )
                    }
                    lastTickTimestamp = SystemClock.elapsedRealtime()
                }
            }

            GameStatus.PAUSED -> {
                // Tapping while paused resumes the game
                resumeGame()
            }

            GameStatus.GAME_OVER -> {
                // No action on tap when game over, wait for reset
            }
        }
    }

    fun togglePauseResume() {
        val state = _uiState.value
        when (state.gameStatus) {
            GameStatus.RUNNING -> pauseGame()
            GameStatus.PAUSED -> resumeGame()
            GameStatus.NOT_STARTED -> {
                // If not started, start with Player 1 (White to move)
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.RUNNING,
                        activePlayer = PlayerSide.PLAYER_1
                    )
                }
                startTimerLoop()
            }
            GameStatus.GAME_OVER -> resetGame()
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameStatus == GameStatus.RUNNING) {
            stopTimerLoop()
            _uiState.update { it.copy(gameStatus = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameStatus == GameStatus.PAUSED) {
            _uiState.update { it.copy(gameStatus = GameStatus.RUNNING) }
            startTimerLoop()
        }
    }

    fun resetGame() {
        stopTimerLoop()
        val currentTc = _uiState.value.timeControl
        _uiState.update { current ->
            current.copy(
                gameStatus = GameStatus.NOT_STARTED,
                activePlayer = null,
                winner = null,
                player1 = PlayerState(
                    side = PlayerSide.PLAYER_1,
                    name = "Player 1",
                    timeRemainingMillis = currentTc.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                player2 = PlayerState(
                    side = PlayerSide.PLAYER_2,
                    name = "Player 2",
                    timeRemainingMillis = currentTc.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                )
            )
        }
    }

    fun setTimeControl(timeControl: TimeControl) {
        stopTimerLoop()
        _uiState.update { current ->
            current.copy(
                timeControl = timeControl,
                gameStatus = GameStatus.NOT_STARTED,
                activePlayer = null,
                winner = null,
                player1 = PlayerState(
                    side = PlayerSide.PLAYER_1,
                    name = "Player 1",
                    timeRemainingMillis = timeControl.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                player2 = PlayerState(
                    side = PlayerSide.PLAYER_2,
                    name = "Player 2",
                    timeRemainingMillis = timeControl.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                isCustomDialogOpen = false
            )
        }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun openCustomDialog() {
        pauseGame()
        _uiState.update { it.copy(isCustomDialogOpen = true) }
    }

    fun closeCustomDialog() {
        _uiState.update { it.copy(isCustomDialogOpen = false) }
    }

    private fun startTimerLoop() {
        stopTimerLoop()
        lastTickTimestamp = SystemClock.elapsedRealtime()

        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.gameStatus == GameStatus.RUNNING) {
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTickTimestamp
                lastTickTimestamp = now

                val currentState = _uiState.value
                val active = currentState.activePlayer

                if (active != null && delta > 0) {
                    val activePlayerState = if (active == PlayerSide.PLAYER_1) currentState.player1 else currentState.player2
                    val newRemaining = (activePlayerState.timeRemainingMillis - delta).coerceAtLeast(0L)

                    if (newRemaining <= 0L) {
                        // Flag fell!
                        handleTimeOut(active)
                        break
                    } else {
                        // Check warning beep
                        if (activePlayerState.timeRemainingMillis > 10_000L && newRemaining <= 10_000L) {
                            audioHapticHelper?.playWarningSound(currentState.soundEnabled)
                        }

                        _uiState.update { curr ->
                            if (active == PlayerSide.PLAYER_1) {
                                curr.copy(player1 = curr.player1.copy(timeRemainingMillis = newRemaining))
                            } else {
                                curr.copy(player2 = curr.player2.copy(timeRemainingMillis = newRemaining))
                            }
                        }
                    }
                }

                delay(50L) // smooth 20fps update for tenths/milliseconds
            }
        }
    }

    private fun handleTimeOut(flaggedPlayer: PlayerSide) {
        stopTimerLoop()
        val winnerSide = if (flaggedPlayer == PlayerSide.PLAYER_1) PlayerSide.PLAYER_2 else PlayerSide.PLAYER_1
        
        val currentState = _uiState.value
        audioHapticHelper?.triggerTimeOutHaptic(currentState.hapticEnabled)
        audioHapticHelper?.playTimeOutSound(currentState.soundEnabled)

        _uiState.update { curr ->
            curr.copy(
                gameStatus = GameStatus.GAME_OVER,
                winner = winnerSide,
                player1 = if (flaggedPlayer == PlayerSide.PLAYER_1) curr.player1.copy(timeRemainingMillis = 0L, isFlagged = true) else curr.player1,
                player2 = if (flaggedPlayer == PlayerSide.PLAYER_2) curr.player2.copy(timeRemainingMillis = 0L, isFlagged = true) else curr.player2
            )
        }
    }

    private fun stopTimerLoop() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimerLoop()
        audioHapticHelper?.release()
    }
}
