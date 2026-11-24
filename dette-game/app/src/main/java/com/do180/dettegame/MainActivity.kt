package com.do180.dettegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.do180.dettegame.ui.GameScreen
import com.do180.dettegame.ui.GameViewModel
import com.do180.dettegame.ui.theme.DetteGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DetteGameTheme {
                val viewModel: GameViewModel = viewModel()
                GameScreen(viewModel = viewModel)
            }
        }
    }
}
