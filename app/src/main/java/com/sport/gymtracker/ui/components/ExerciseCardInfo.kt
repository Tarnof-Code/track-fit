package com.sport.gymtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.data.local.canToggleExerciseSetSequentially
import kotlin.math.min

private val CompletedSetGreen = Color(0xFFC8E6C9)
private val CompletedSetOnGreen = Color(0xFF1B5E20)

data class SessionSetProgressUi(
    val plannedSets: Int,
    val completedMask: Long,
    val onSetClick: (setIndex: Int) -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseCardInfoContent(
    name: String,
    notes: String = "",
    /** Affiche l’icône épingle à côté du titre ; ouvre les notes au clic. */
    onNotesClick: () -> Unit = {},
    exerciseTypeLabel: String,
    prescriptionLine: String,
    intensityLine: String?,
    equipment: String,
    musclesLine: String,
    showRestBetweenSets: Boolean,
    restBetweenSetsSeconds: Int,
    /** Séance en cours : une puce par série ; le repos se lance depuis ces puces. */
    sessionSetProgress: SessionSetProgressUi? = null,
    modifier: Modifier = Modifier,
) {
    val prescriptionAboveChips =
        sessionSetProgress != null && sessionSetProgress.plannedSets > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
            )
            if (notes.isNotBlank()) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = "Afficher les notes",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onNotesClick),
                )
            }
        }
        Text(
            exerciseTypeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!prescriptionAboveChips) {
            Text(prescriptionLine, style = MaterialTheme.typography.bodyMedium)
        }
        intensityLine?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        if (equipment.isNotBlank()) {
            Text("Matériel : $equipment", style = MaterialTheme.typography.bodySmall)
        }
        if (musclesLine.isNotBlank()) {
            Text("Muscles : $musclesLine", style = MaterialTheme.typography.bodySmall)
        }
        if (sessionSetProgress != null && sessionSetProgress.plannedSets > 0) {
            val total = sessionSetProgress.plannedSets
            val rowCount = (total + 4) / 5
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(prescriptionLine, style = MaterialTheme.typography.bodyMedium)
                repeat(rowCount) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val start = rowIndex * 5
                        val end = min(start + 5, total)
                        for (idx in start until end) {
                            val completed =
                                (sessionSetProgress.completedMask and (1L shl idx)) != 0L
                            val chipEnabled = canToggleExerciseSetSequentially(
                                idx,
                                sessionSetProgress.completedMask,
                                total,
                            )
                            FilterChip(
                                selected = completed,
                                onClick = { sessionSetProgress.onSetClick(idx) },
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${idx + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                },
                                leadingIcon = null,
                                enabled = chipEnabled,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CompletedSetGreen,
                                    selectedLabelColor = CompletedSetOnGreen,
                                ),
                            )
                        }
                    }
                }
            }
        }
        if (showRestBetweenSets && restBetweenSetsSeconds > 0) {
            Text(
                "Repos entre séries : ${restBetweenSetsSeconds}s",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    top = if (prescriptionAboveChips) {
                        6.dp
                    } else {
                        0.dp
                    },
                ),
            )
        }
    }
}
