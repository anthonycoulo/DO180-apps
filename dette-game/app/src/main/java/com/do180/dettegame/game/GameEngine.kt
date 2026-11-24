package com.do180.dettegame.game

import kotlin.random.Random

class GameEngine(private val random: Random = Random()) {

    fun newGame(): GameState = GameState(
        year = 1,
        debtBillions = 2850.0,
        gdpBillions = 2800.0,
        deficitPct = 5.0,
        growthPct = 1.0,
        interestRatePct = 2.0,
        approvalPct = 50.0
    )

    fun applyDecision(state: GameState, decision: Decision): GameState {
        if (state.status != Status.ONGOING) return state

        var deficit = state.deficitPct
        var growth = state.growthPct
        var approval = state.approvalPct
        var rate = state.interestRatePct
        var extraDebt = 0.0

        when (decision) {
            Decision.AUSTERITY -> {
                deficit -= 4.0
                growth -= 0.6
                approval -= 4.0
            }
            Decision.STIMULUS -> {
                deficit += 2.0
                growth += 1.5
                approval += 2.0
            }
            Decision.RAISE_TAXES -> {
                deficit -= 2.5
                growth -= 0.3
                approval -= 6.0
            }
            Decision.CUT_TAXES -> {
                deficit += 1.5
                growth += 0.7
                approval += 6.0
            }
            Decision.BORROW -> {
                extraDebt += state.gdpBillions * 0.02
                rate += if (state.debtToGdpPct > 100.0) 0.3 else 0.1
                approval += 1.0
            }
            Decision.WAIT -> {
                deficit += 0.2
                growth -= 0.1
            }
        }

        val event = rollEvent()
        deficit += event.deficitDelta
        growth += event.growthDelta
        rate += event.rateDelta
        approval += event.approvalDelta

        growth = growth.coerceIn(-5.0, 6.0)
        rate = rate.coerceAtLeast(0.5)
        deficit = deficit.coerceIn(-3.0, 15.0)
        approval = (approval + growth * 2.0).coerceIn(0.0, 100.0)

        val newGdp = state.gdpBillions * (1.0 + growth / 100.0)
        val newDebt = state.debtBillions +
            state.debtBillions * rate / 100.0 +
            deficit / 100.0 * newGdp +
            extraDebt
        val ratio = newDebt / newGdp * 100.0

        var next = state.copy(
            debtBillions = newDebt,
            gdpBillions = newGdp,
            deficitPct = deficit,
            growthPct = growth,
            interestRatePct = rate,
            approvalPct = approval,
            events = (state.events + GameEvent(state.year, event.title, event.message)).takeLast(30)
        )

        next = when {
            ratio >= GameState.INSOLVENCY_RATIO_PCT ->
                next.copy(status = Status.LOST_INSOLVENCY)
            approval <= 0.0 ->
                next.copy(status = Status.LOST_OUSTED)
            state.year >= state.mandateLength ->
                if (ratio < GameState.WIN_RATIO_PCT && approval >= GameState.MIN_APPROVAL_PCT) {
                    next.copy(status = Status.WON)
                } else {
                    next.copy(status = Status.LOST_MANDATE)
                }
            else -> next.copy(year = state.year + 1)
        }
        return next
    }

    fun score(state: GameState): Int =
        (((GameState.INSOLVENCY_RATIO_PCT - state.debtToGdpPct) * 10.0) +
            state.approvalPct +
            state.growthPct * 50.0).toInt().coerceAtLeast(0)

    private fun rollEvent(): RandomEvent =
        if (random.nextDouble() < EVENT_PROBABILITY) {
            ALL_EVENTS[random.nextInt(ALL_EVENTS.size)]
        } else {
            CALM_EVENT
        }

    private data class RandomEvent(
        val title: String,
        val message: String,
        val deficitDelta: Double,
        val growthDelta: Double,
        val rateDelta: Double,
        val approvalDelta: Double
    )

    private companion object {
        const val EVENT_PROBABILITY = 0.30

        val CALM_EVENT = RandomEvent(
            "Année calme",
            "Rien de particulier à signaler cette année.",
            0.0, 0.1, 0.0, 0.0
        )

        val ALL_EVENTS = listOf(
            RandomEvent(
                "Crise financière mondiale",
                "Les marchés paniquent : les taux montent et la croissance s'effondre.",
                1.0, -1.5, 0.5, -3.0
            ),
            RandomEvent(
                "Boom technologique",
                "Les startups françaises cartonnent à l'international.",
                -0.5, 1.0, 0.0, 2.0
            ),
            RandomEvent(
                "Pandémie",
                "Confinements et plans de soutien massifs.",
                2.0, -2.0, 0.2, -5.0
            ),
            RandomEvent(
                "Inflation élevée",
                "La banque centrale resserre le crédit.",
                0.3, -0.3, 0.4, -2.0
            ),
            RandomEvent(
                "Plan de transition écologique",
                "Investissements verts subventionnés par l'État.",
                1.0, 0.4, 0.0, 3.0
            ),
            RandomEvent(
                "Tensions géopolitiques",
                "Dépenses militaires et incertitude économique.",
                1.2, -0.8, 0.3, -2.0
            )
        )
    }
}
