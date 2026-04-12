package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.ui.components.MuscleFrequencyBars
import com.sport.gymtracker.ui.viewmodel.StatisticsViewModel
import java.util.Locale
import kotlin.math.roundToInt

private enum class AvgPeriod { WEEK, MONTH }

@Composable
fun StatisticsScreen(onOpenExerciseProgress: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: StatisticsViewModel = viewModel(factory = StatisticsViewModel.factory(app))
    val stats by vm.stats.collectAsState()
    val scroll = rememberScrollState()
    var sessionPeriod by remember { mutableStateOf(AvgPeriod.WEEK) }
    var gymTimePeriod by remember { mutableStateOf(AvgPeriod.WEEK) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Stats", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Semaine lundi–dimanche, moyennes sur tout l’historique et muscles sollicités.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!stats.hasData) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "Terminez au moins une séance pour afficher les statistiques.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Semaine en cours", style = MaterialTheme.typography.titleMedium)
                    Text(
                        stats.weekRangeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${stats.currentWeekSessionCount} séance(s)",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (stats.currentWeekSessions.isEmpty()) {
                        Text(
                            "Aucune séance terminée cette semaine.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        stats.currentWeekSessions.forEach { row ->
                            Text(
                                "• ${row.dayLabel} — ${row.templateOrTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nombre moyen de séances", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Moyenne sur toute la période depuis votre première séance enregistrée.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = sessionPeriod == AvgPeriod.WEEK,
                            onClick = { sessionPeriod = AvgPeriod.WEEK },
                            label = { Text("Par semaine") },
                        )
                        FilterChip(
                            selected = sessionPeriod == AvgPeriod.MONTH,
                            onClick = { sessionPeriod = AvgPeriod.MONTH },
                            label = { Text("Par mois") },
                        )
                    }
                    val sessionsAvg = when (sessionPeriod) {
                        AvgPeriod.WEEK -> stats.avgSessionsPerWeek
                        AvgPeriod.MONTH -> stats.avgSessionsPerMonth
                    }
                    Text(
                        String.format(Locale.FRENCH, "%.1f", sessionsAvg),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Temps moyen à la salle", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Durée des séances terminées (début → fin), moyenne sur tout l’historique.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = gymTimePeriod == AvgPeriod.WEEK,
                            onClick = { gymTimePeriod = AvgPeriod.WEEK },
                            label = { Text("Par semaine") },
                        )
                        FilterChip(
                            selected = gymTimePeriod == AvgPeriod.MONTH,
                            onClick = { gymTimePeriod = AvgPeriod.MONTH },
                            label = { Text("Par mois") },
                        )
                    }
                    val minutes = when (gymTimePeriod) {
                        AvgPeriod.WEEK -> stats.avgGymMinutesPerWeek
                        AvgPeriod.MONTH -> stats.avgGymMinutesPerMonth
                    }
                    Text(
                        formatMinutesFr(minutes),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Fréquence des muscles", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Nombre d’exercices par groupe musculaire.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MuscleFrequencyBars(items = stats.muscleFrequencies)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenExerciseProgress),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Progression par exercice", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Courbes : répétitions, charge, durées…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun formatMinutesFr(minutes: Float): String {
    if (minutes.isNaN() || minutes <= 0f) return "—"
    val total = minutes.roundToInt()
    val h = total / 60
    val m = total % 60
    return when {
        h > 0 && m > 0 -> "$h h $m min"
        h > 0 -> "$h h"
        else -> "$m min"
    }
}
