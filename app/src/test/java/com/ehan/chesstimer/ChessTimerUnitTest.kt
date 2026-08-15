package com.ehan.chesstimer

import com.ehan.chesstimer.data.ChessColor
import com.ehan.chesstimer.data.GameStatus
import com.ehan.chesstimer.data.PlayerSide
import com.ehan.chesstimer.data.TimeControl
import com.ehan.chesstimer.viewmodel.ChessTimerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChessTimerUnitTest {

    @Test
    fun testTimeControlDefaults() {
        val defaultTc = TimeControl.DEFAULT
        assertEquals("Blitz 5+0", defaultTc.name)
        assertEquals(5 * 60_000L, defaultTc.initialTimeMillis)
        assertEquals(0L, defaultTc.incrementMillis)
    }

    @Test
    fun testViewModelInitialState() {
        val viewModel = ChessTimerViewModel()
        val state = viewModel.uiState.value

        assertEquals(GameStatus.NOT_STARTED, state.gameStatus)
        assertEquals(ChessColor.WHITE, state.player1.color)
        assertEquals(ChessColor.BLACK, state.player2.color)
        assertEquals(TimeControl.DEFAULT.initialTimeMillis, state.player1.timeRemainingMillis)
        assertEquals(TimeControl.DEFAULT.initialTimeMillis, state.player2.timeRemainingMillis)
        assertEquals(0, state.player1.moveCount)
        assertEquals(0, state.player2.moveCount)
    }

    @Test
    fun testSwapSides() {
        val viewModel = ChessTimerViewModel()
        viewModel.swapSides()

        val state = viewModel.uiState.value
        assertEquals(ChessColor.BLACK, state.player1.color)
        assertEquals(ChessColor.WHITE, state.player2.color)

        // Swap back
        viewModel.swapSides()
        val state2 = viewModel.uiState.value
        assertEquals(ChessColor.WHITE, state2.player1.color)
        assertEquals(ChessColor.BLACK, state2.player2.color)
    }

    @Test
    fun testFirstTapStartsGame() {
        val viewModel = ChessTimerViewModel()
        // Player 1 taps to make first move -> Player 2's clock starts
        viewModel.onPlayerTap(PlayerSide.PLAYER_1)

        val state = viewModel.uiState.value
        assertEquals(GameStatus.RUNNING, state.gameStatus)
        assertEquals(PlayerSide.PLAYER_2, state.activePlayer)
        assertEquals(1, state.player1.moveCount)
    }

    @Test
    fun testSetCustomTimeControl() {
        val viewModel = ChessTimerViewModel()
        val custom = TimeControl(
            name = "Custom 3+2",
            initialTimeMillis = 3 * 60_000L,
            incrementMillis = 2_000L
        )
        viewModel.setTimeControl(custom)

        val state = viewModel.uiState.value
        assertEquals(3 * 60_000L, state.player1.timeRemainingMillis)
        assertEquals(3 * 60_000L, state.player2.timeRemainingMillis)
        assertEquals("Custom 3+2", state.timeControl.name)
    }
}
