package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.data.local.ExercisePerformanceHistoryRow
import com.sport.gymtracker.domain.parseSingleLoadKg
import com.sport.gymtracker.ui.components.ProgressLineChart
import com.sport.gymtracker.ui.viewmodel.ExerciseProgressDetailViewModel
import com.sport.gymtracker.util.FrenchDateTime

private enum class ProgressChartMetric(val labelFr: String) {
    REPS("Répétitions / série"),
    LOAD_KG("Charge (kg)"),
    DURATION_SEC("Durée (s) / série"),
    DURATION_MIN("Durée (min) / série"),
    SETS("Nombre de séries"),
}

private fun ExercisePerformanceHistoryRow.metricValue(m: ProgressChartMetric): Float? =
    when (m) {
        ProgressChartMetric.REPS -> perfRepsPerSet?.toFloat()
        ProgressChartMetric.LOAD_KG -> perfLoadKg ?: parseSingleLoadKg(perfLoadSpec)
        ProgressChartMetric.DURATION_SEC -> perfDurationSecondsPerSet?.toFloat()
        ProgressChartMetric.DURATION_MIN -> perfDurationMinutesPerSet?.toFloat()
        ProgressChartMetric.SETS -> perfSets?.toFloat()
    }

private fun metricsWithData(history: List<ExercisePerformanceHistoryRow>): List<ProgressChartMetric> =
    ProgressChartMetric.entries.filter { m -> history.any { row -> row.metricValue(m) != null } }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseProgressDetailScreen(
    blueprintId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: ExerciseProgressDetailViewModel = viewModel(
        factory = ExerciseProgressDetailViewModel.Factory(app, blueprintId),
    )
    val ui by vm.ui.collectAsState()
    var selectedMetric by remember { mutableStateOf<ProgressChartMetric?>(null) }

    val history = ui?.history.orEmpty()
    val available = remember(history) { metricsWithData(history) }

    LaunchedEffect(available) {
        if (available.isEmpty()) {
            selectedMetric = null
        } else if (selectedMetric == null || selectedMetric !in available) {
            selectedMetric = available.first()
        }
    }

    val chartPoints = remember(history, selectedMetric) {
        val m = selectedMetric ?: return@remember emptyList()
        history.mapNotNull { row ->
            val y = row.metricValue(m) ?: return@mapNotNull null
            val label = FrenchDateTime.formatChartDay(row.sessionStartMillis)
            label to y
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ui?.blueprintName ?: "Exercice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        if (ui == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {
                Text("Exercice introuvable.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Évolution des valeurs enregistrées à la fin de chaque séance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (available.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(
                            "Aucune mesure numérique pour cet exercice.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        available.forEach { m ->
                            FilterChip(
                                selected = m == selectedMetric,
                                onClick = { selectedMetric = m },
                                label = { Text(m.labelFr) },
                            )
                        }
                    }

                    selectedMetric?.let { m ->
                        Text(m.labelFr, style = MaterialTheme.typography.titleSmall)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ProgressLineChart(
                                points = chartPoints,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }

                val hasReglage = history.any { !it.perfRowResistance.isNullOrBlank() }
                if (hasReglage) {
                    Text("Réglage / niveau (texte)", style = MaterialTheme.typography.titleSmall)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            history.filter { !it.perfRowResistance.isNullOrBlank() }.forEach { row ->
                                val d = FrenchDateTime.formatChartDay(row.sessionStartMillis)
                                Text(
                                    "$d — ${row.perfRowResistance}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
