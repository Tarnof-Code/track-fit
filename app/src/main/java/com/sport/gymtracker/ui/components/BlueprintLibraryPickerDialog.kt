package com.sport.gymtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.data.local.ExerciseBlueprintEntity
import com.sport.gymtracker.domain.prescriptionSummaryShort

@Composable
fun BlueprintLibraryPickerDialog(
    exerciseBlueprints: List<ExerciseBlueprintEntity>,
    emptyLibraryMessage: String,
    selectedIds: List<Long>,
    onToggleBlueprintId: (Long) -> Unit,
    onDismiss: () -> Unit,
    onConfirmAdd: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bibliothèque d’exercices") },
        text = {
            if (exerciseBlueprints.isEmpty()) {
                Text(
                    emptyLibraryMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Coche un ou plusieurs exercices, puis appuie sur « Ajouter ». L’ordre suit tes coches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        exerciseBlueprints.forEach { bp ->
                            val selected = bp.id in selectedIds
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleBlueprintId(bp.id) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = null,
                                    )
                                    Column(Modifier.padding(start = 4.dp)) {
                                        Text(bp.name, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            bp.prescriptionSummaryShort(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmAdd,
                enabled = selectedIds.isNotEmpty(),
            ) {
                val n = selectedIds.size
                Text(if (n > 1) "Ajouter ($n)" else "Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        },
    )
}
