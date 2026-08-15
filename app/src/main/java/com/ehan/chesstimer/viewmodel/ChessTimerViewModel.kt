package com.ehan.chesstimer.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehan.chesstimer.audio.AudioHapticHelper
import com.ehan.chesstimer.data.ChessColor
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
        color = ChessColor.WHITE,
        timeRemainingMillis = TimeControl.DEFAULT.initialTimeMillis
    ),
    val player2: PlayerState = PlayerState(
        side = PlayerSide.PLAYER_2,
        name = "Player 2",
        color = ChessColor.BLACK,
        timeRemainingMillis = TimeControl.DEFAULT.initialTimeMillis
    ),
    val activePlayer: PlayerSide? = null,
    val gameStatus: GameStatus = GameStatus.NOT_STARTED,
    val winner: PlayerSide? = null,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val showTenthsUnderSeconds: Int = 20, // show tenths when < 20s
    val isCustomDialogOpen: Boolean = false,
    val isShuffleModalOpen: Boolean = false,
    val isShuffling: Boolean = false,
    val shuffleDisplayColorP1: ChessColor = ChessColor.WHITE,
    val shuffleResultSummary: String? = null
)

class ChessTimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var shuffleJob: Job? = null
    private var lastTickTimestamp: Long = 0L
    private var audioHapticHelper: AudioHapticHelper? = null

    fun setAudioHapticHelper(helper: AudioHapticHelper) {
        this.audioHapticHelper = helper
    }

    /**
     * Handles tap on a player zone.
     * If the game has not started, tapping starts the clock for the opponent (first move completed).
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
                // If not started, start with whichever player has White
                val whitePlayerSide = if (state.player1.color == ChessColor.WHITE) PlayerSide.PLAYER_1 else PlayerSide.PLAYER_2
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.RUNNING,
                        activePlayer = whitePlayerSide
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
        val p1Color = _uiState.value.player1.color
        val p2Color = _uiState.value.player2.color
        _uiState.update { current ->
            current.copy(
                gameStatus = GameStatus.NOT_STARTED,
                activePlayer = null,
                winner = null,
                player1 = PlayerState(
                    side = PlayerSide.PLAYER_1,
                    name = "Player 1",
                    color = p1Color,
                    timeRemainingMillis = currentTc.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                player2 = PlayerState(
                    side = PlayerSide.PLAYER_2,
                    name = "Player 2",
                    color = p2Color,
                    timeRemainingMillis = currentTc.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                )
            )
        }
    }

    fun setTimeControl(timeControl: TimeControl) {
        stopTimerLoop()
        val p1Color = _uiState.value.player1.color
        val p2Color = _uiState.value.player2.color
        _uiState.update { current ->
            current.copy(
                timeControl = timeControl,
                gameStatus = GameStatus.NOT_STARTED,
                activePlayer = null,
                winner = null,
                player1 = PlayerState(
                    side = PlayerSide.PLAYER_1,
                    name = "Player 1",
                    color = p1Color,
                    timeRemainingMillis = timeControl.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                player2 = PlayerState(
                    side = PlayerSide.PLAYER_2,
                    name = "Player 2",
                    color = p2Color,
                    timeRemainingMillis = timeControl.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                isCustomDialogOpen = false
            )
        }
    }

    /**
     * Triggers animated shuffle of player sides (White ⇄ Black)
     */
    fun startRandomizeAnimation() {
        if (_uiState.value.gameStatus == GameStatus.RUNNING) {
            pauseGame()
        }
        shuffleJob?.cancel()

        _uiState.update {
            it.copy(
                isShuffleModalOpen = true,
                isShuffling = true,
                shuffleResultSummary = null,
                shuffleDisplayColorP1 = it.player1.color
            )
        }

        shuffleJob = viewModelScope.launch {
            var currentP1 = _uiState.value.player1.color
            val stepDelays = listOf(
                45L, 45L, 50L, 50L, 60L, 70L, 85L, 100L, 130L, 170L, 220L, 280L, 360L, 460L
            )

            for (delayMs in stepDelays) {
                currentP1 = currentP1.opponent
                _uiState.update {
                    it.copy(shuffleDisplayColorP1 = currentP1)
                }
                audioHapticHelper?.playShuffleTick(_uiState.value.soundEnabled)
                audioHapticHelper?.triggerShuffleTickHaptic(_uiState.value.hapticEnabled)
                delay(delayMs)
            }

            // Final 50/50 random selection
            val finalP1Color = if (kotlin.random.Random.nextBoolean()) ChessColor.WHITE else ChessColor.BLACK
            val finalP2Color = finalP1Color.opponent

            audioHapticHelper?.playShuffleDoneSound(_uiState.value.soundEnabled)
            audioHapticHelper?.triggerShuffleDoneHaptic(_uiState.value.hapticEnabled)

            val summary = if (finalP1Color == ChessColor.WHITE) {
                "Pemain 1 (Bawah) mendapatkan PUTIH ♔ & melangkah duluan!"
            } else {
                "Pemain 2 (Atas) mendapatkan PUTIH ♔ & melangkah duluan!"
            }

            _uiState.update { current ->
                current.copy(
                    isShuffling = false,
                    shuffleDisplayColorP1 = finalP1Color,
                    shuffleResultSummary = summary,
                    gameStatus = GameStatus.NOT_STARTED,
                    activePlayer = null,
                    winner = null,
                    player1 = current.player1.copy(
                        color = finalP1Color,
                        timeRemainingMillis = current.timeControl.initialTimeMillis,
                        moveCount = 0,
                        isFlagged = false
                    ),
                    player2 = current.player2.copy(
                        color = finalP2Color,
                        timeRemainingMillis = current.timeControl.initialTimeMillis,
                        moveCount = 0,
                        isFlagged = false
                    )
                )
            }
        }
    }

    /**
     * Manually swaps White and Black between Player 1 and Player 2
     */
    fun swapSides() {
        val nextP1Color = _uiState.value.player1.color.opponent
        val nextP2Color = nextP1Color.opponent

        audioHapticHelper?.triggerTapHaptic(_uiState.value.hapticEnabled)
        audioHapticHelper?.playClickSound(_uiState.value.soundEnabled)

        _uiState.update { current ->
            current.copy(
                gameStatus = GameStatus.NOT_STARTED,
                activePlayer = null,
                winner = null,
                player1 = current.player1.copy(
                    color = nextP1Color,
                    timeRemainingMillis = current.timeControl.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                player2 = current.player2.copy(
                    color = nextP2Color,
                    timeRemainingMillis = current.timeControl.initialTimeMillis,
                    moveCount = 0,
                    isFlagged = false
                ),
                shuffleDisplayColorP1 = nextP1Color
            )
        }
    }

    fun dismissShuffleModal() {
        _uiState.update { it.copy(isShuffleModalOpen = false) }
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
