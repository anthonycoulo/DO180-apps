package com.do180.dettegame

import com.do180.dettegame.game.Decision
import com.do180.dettegame.game.GameEngine
import com.do180.dettegame.game.GameState
import com.do180.dettegame.game.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    @Test
    fun newGameStartsAtYearOneAroundFrenchDebtLevel() {
        val state = GameEngine(Random(1)).newGame()
        assertEquals(1, state.year)
        assertEquals(Status.ONGOING, state.status)
        assertEquals(101.8, state.debtToGdpPct, 0.1)
        assertEquals(50.0, state.approvalPct, 0.001)
    }

    @Test
    fun austerityReducesDeficitMoreThanStimulus() {
        val initial = GameEngine(Random(7)).newGame()
        val austere = GameEngine(Random(7)).applyDecision(initial, Decision.AUSTERITY)
        val stimulus = GameEngine(Random(7)).applyDecision(initial, Decision.STIMULUS)
        assertTrue(austere.deficitPct < initial.deficitPct)
        assertTrue(austere.deficitPct < stimulus.deficitPct)
    }

    @Test
    fun debtGrowsWithInterestEvenWhenWaiting() {
        val engine = GameEngine(Random(3))
        val initial = engine.newGame()
        val next = engine.applyDecision(initial, Decision.WAIT)
        assertTrue(next.debtBillions > initial.debtBillions)
    }

    @Test
    fun engineIsDeterministicForSameSeed() {
        val engineA = GameEngine(Random(42))
        val engineB = GameEngine(Random(42))
        var stateA = engineA.newGame()
        var stateB = engineB.newGame()
        repeat(10) {
            stateA = engineA.applyDecision(stateA, Decision.WAIT)
            stateB = engineB.applyDecision(stateB, Decision.WAIT)
        }
        assertEquals(stateA, stateB)
    }

    @Test
    fun insolvencyWhenDebtRatioExplodes() {
        val engine = GameEngine(Random(5))
        val state = engine.newGame().copy(debtBillions = 4200.0)
        val next = engine.applyDecision(state, Decision.WAIT)
        assertEquals(Status.LOST_INSOLVENCY, next.status)
    }

    @Test
    fun relentlessStimulusLeadsToInsolvency() {
        val engine = GameEngine(Random(7))
        var state = engine.newGame()
        while (state.status == Status.ONGOING) {
            state = engine.applyDecision(state, Decision.STIMULUS)
        }
        assertEquals(Status.LOST_INSOLVENCY, state.status)
    }

    @Test
    fun pureAusterityGetsYouOusted() {
        val engine = GameEngine(Random(0))
        var state = engine.newGame()
        while (state.status == Status.ONGOING) {
            state = engine.applyDecision(state, Decision.AUSTERITY)
        }
        assertEquals(Status.LOST_OUSTED, state.status)
    }

    @Test
    fun balancedStrategyCanWinTheMandate() {
        val engine = GameEngine(Random(1))
        var state = engine.newGame()
        while (state.status == Status.ONGOING) {
            val decision = when {
                state.deficitPct > 1.5 -> Decision.RAISE_TAXES
                state.deficitPct > 0.0 -> Decision.AUSTERITY
                else -> Decision.STIMULUS
            }
            state = engine.applyDecision(state, decision)
        }
        assertEquals(Status.WON, state.status)
        assertTrue(state.debtToGdpPct < GameState.WIN_RATIO_PCT)
    }

    @Test
    fun gameAlwaysEndsWithinTheMandate() {
        val engine = GameEngine(Random(3))
        var state = engine.newGame()
        var turns = 0
        while (state.status == Status.ONGOING && turns < 20) {
            state = engine.applyDecision(state, Decision.WAIT)
            turns++
        }
        assertNotEquals(Status.ONGOING, state.status)
        assertTrue(turns <= state.mandateLength)
    }

    @Test
    fun scoreIsPositiveForAFreshGame() {
        val engine = GameEngine(Random(1))
        assertTrue(engine.score(engine.newGame()) > 0)
    }
}
