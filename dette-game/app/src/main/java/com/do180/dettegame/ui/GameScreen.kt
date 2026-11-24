package com.do180.dettegame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.do180.dettegame.game.Decision
import com.do180.dettegame.game.GameState
import com.do180.dettegame.game.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("La Dette de la République") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.status == Status.ONGOING) {
                OngoingGame(state = state, onDecision = viewModel::choose)
            } else {
                EndScreen(state = state, score = viewModel.score(), onRestart = viewModel::restart)
            }
        }
    }
}

@Composable
private fun OngoingGame(state: GameState, onDecision: (Decision) -> Unit) {
    Text(
        text = "Année ${state.year} / ${state.mandateLength}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Objectif : ramener la dette sous 100 % du PIB en ${state.mandateLength} ans sans être renversé.",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IndicatorsCard(state)
        }
        item {
            Text(
                text = "Votre décision pour l'année ${state.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        items(Decision.entries.toList()) { decision ->
            DecisionButton(decision = decision, onClick = { onDecision(decision) })
        }
        if (state.events.isNotEmpty()) {
            item {
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(state.events.reversed()) { event ->
                Text(
                    text = "Année ${event.year} — ${event.title} : ${event.message}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun IndicatorsCard(state: GameState) {
    val ratioColor = when {
        state.debtToGdpPct >= GameState.INSOLVENCY_RATIO_PCT -> Color(0xFFB00020)
        state.debtToGdpPct >= 120.0 -> Color(0xFFB00020)
        state.debtToGdpPct >= GameState.WIN_RATIO_PCT -> Color(0xFFE8890C)
        else -> Color(0xFF1E8E3E)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            IndicatorRow("Dette", "${state.debtBillions.toInt()} Md€")
            IndicatorRow("PIB", "${state.gdpBillions.toInt()} Md€")
            IndicatorRow("Dette / PIB", "${"%.1f".format(state.debtToGdpPct)} %", ratioColor)
            IndicatorRow("Déficit public", "${"%.1f".format(state.deficitPct)} % du PIB")
            IndicatorRow("Croissance", "${"%.1f".format(state.growthPct)} %")
            IndicatorRow("Taux d'intérêt moyen", "${"%.1f".format(state.interestRatePct)} %")
            IndicatorRow("Cote de popularité", "${"%.0f".format(state.approvalPct)} %")
        }
    }
}

@Composable
private fun IndicatorRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun DecisionButton(decision: Decision, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = decision.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = decision.description,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                Text("Choisir")
            }
        }
    }
}

@Composable
private fun EndScreen(state: GameState, score: Int, onRestart: () -> Unit) {
    val (title, message) = when (state.status) {
        Status.WON -> "Victoire !" to
            "La dette est passée sous 100 % du PIB (${"%.1f".format(state.debtToGdpPct)} %) avec une cote de ${"%.0f".format(state.approvalPct)} %. La République vous remercie."
        Status.LOST_MANDATE -> "Mandat terminé" to
            "Objectif non atteint : dette à ${"%.1f".format(state.debtToGdpPct)} % du PIB après ${state.mandateLength} ans."
        Status.LOST_INSOLVENCY -> "Défaut de paiement" to
            "La dette a atteint ${"%.1f".format(state.debtToGdpPct)} % du PIB en année ${state.year}. Les marchés ne vous prêtent plus."
        Status.LOST_OUSTED -> "Renversé !" to
            "Votre cote est tombée à 0 % en année ${state.year}. Le gouvernement est dissous."
        Status.ONGOING -> "" to ""
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Score : $score",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRestart) {
            Text("Nouveau mandat")
        }
    }
}
