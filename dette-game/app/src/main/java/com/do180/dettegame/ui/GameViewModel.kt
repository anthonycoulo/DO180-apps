package com.do180.dettegame.ui

import androidx.lifecycle.ViewModel
import com.do180.dettegame.game.Decision
import com.do180.dettegame.game.GameEngine
import com.do180.dettegame.game.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel : ViewModel() {
    private val engine = GameEngine()
    private val _state = MutableStateFlow(engine.newGame())
    val state: StateFlow<GameState> = _state

    fun choose(decision: Decision) {
        _state.value = engine.applyDecision(_state.value, decision)
    }

    fun score(): Int = engine.score(_state.value)

    fun restart() {
        _state.value = engine.newGame()
    }
}
