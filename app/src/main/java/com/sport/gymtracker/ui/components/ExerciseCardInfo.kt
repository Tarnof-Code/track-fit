package com.sport.gymtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExerciseCardInfoContent(
    name: String,
    exerciseTypeLabel: String,
    prescriptionLine: String,
    intensityLine: String?,
    equipment: String,
    musclesLine: String,
    showRestBetweenSets: Boolean,
    restBetweenSetsSeconds: Int,
    /** Séance en cours : bouton chrono au lieu du texte statique. */
    useRestCountdown: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium)
        Text(
            exerciseTypeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(prescriptionLine, style = MaterialTheme.typography.bodyMedium)
        intensityLine?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        if (equipment.isNotBlank()) {
            Text("Matériel : $equipment", style = MaterialTheme.typography.bodySmall)
        }
        if (musclesLine.isNotBlank()) {
            Text("Muscles : $musclesLine", style = MaterialTheme.typography.bodySmall)
        }
        if (showRestBetweenSets && restBetweenSetsSeconds > 0) {
            if (useRestCountdown) {
                RestBetweenSetsTimer(
                    totalSeconds = restBetweenSetsSeconds,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text(
                    "Repos entre séries : ${restBetweenSetsSeconds}s",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
