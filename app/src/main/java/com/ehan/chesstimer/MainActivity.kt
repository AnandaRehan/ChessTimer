package com.ehan.chesstimer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ehan.chesstimer.audio.AudioHapticHelper
import com.ehan.chesstimer.ui.ChessTimerScreen
import com.ehan.chesstimer.ui.theme.ChessTimerTheme
import com.ehan.chesstimer.ui.theme.DarkBackground
import com.ehan.chesstimer.viewmodel.ChessTimerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChessTimerViewModel by viewModels()
    private lateinit var audioHapticHelper: AudioHapticHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during chess matches
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        audioHapticHelper = AudioHapticHelper(this)
        viewModel.setAudioHapticHelper(audioHapticHelper)

        setContent {
            ChessTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    ChessTimerScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioHapticHelper.release()
    }
}
