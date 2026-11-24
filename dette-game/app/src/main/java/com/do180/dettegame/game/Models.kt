package com.do180.dettegame.game

enum class Decision(val label: String, val description: String) {
    AUSTERITY(
        "Austérité",
        "Réduire les dépenses publiques : -4 pts de déficit, -0,6 pt de croissance, -4 pts de cote"
    ),
    STIMULUS(
        "Relance",
        "Investir dans l'économie : +2 pts de déficit, +1,5 pt de croissance, +2 pts de cote"
    ),
    RAISE_TAXES(
        "Hausse des impôts",
        "Augmenter les prélèvements : -2,5 pts de déficit, -0,3 pt de croissance, -6 pts de cote"
    ),
    CUT_TAXES(
        "Baisses d'impôts",
        "Alléger les prélèvements : +1,5 pt de déficit, +0,7 pt de croissance, +6 pts de cote"
    ),
    BORROW(
        "Emprunter sur les marchés",
        "Émettre des obligations : +2 % du PIB de dette, hausse du taux d'intérêt, +1 pt de cote"
    ),
    WAIT(
        "Attendre",
        "Ne rien changer : dérive des dépenses (+0,2 pt de déficit), -0,1 pt de croissance"
    )
}

enum class Status { ONGOING, WON, LOST_MANDATE, LOST_INSOLVENCY, LOST_OUSTED }

data class GameEvent(val year: Int, val title: String, val message: String)

data class GameState(
    val year: Int,
    val mandateLength: Int = MANDATE_LENGTH,
    val debtBillions: Double,
    val gdpBillions: Double,
    val deficitPct: Double,
    val growthPct: Double,
    val interestRatePct: Double,
    val approvalPct: Double,
    val events: List<GameEvent> = emptyList(),
    val status: Status = Status.ONGOING
) {
    val debtToGdpPct: Double get() = debtBillions / gdpBillions * 100.0

    companion object {
        const val MANDATE_LENGTH = 10
        const val WIN_RATIO_PCT = 100.0
        const val INSOLVENCY_RATIO_PCT = 150.0
        const val MIN_APPROVAL_PCT = 25.0
    }
}
