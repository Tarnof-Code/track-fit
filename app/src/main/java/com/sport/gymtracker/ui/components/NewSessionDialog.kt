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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.data.local.WorkoutTemplateListRow
import java.util.Locale

@Composable
fun NewSessionDialog(
    templateRows: List<WorkoutTemplateListRow>,
    onDismiss: () -> Unit,
    onCreate: (templateId: Long?) -> Unit,
    title: String = "Nouvelle séance",
    confirmLabel: String = "Créer",
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }

    val qNorm = searchQuery.trim().lowercase(Locale.FRENCH)
    val filteredTemplateRows =
        remember(templateRows, qNorm, selectedTemplateId) {
            val filtered =
                if (qNorm.isEmpty()) {
                    templateRows
                } else {
                    templateRows.filter { t ->
                        t.name.lowercase(Locale.FRENCH).contains(qNorm) ||
                            t.description?.lowercase(Locale.FRENCH)?.contains(qNorm) == true
                    }
                }
            val sid = selectedTemplateId
            if (sid == null || filtered.any { it.id == sid }) {
                filtered
            } else {
                val selectedRow = templateRows.find { it.id == sid }
                if (selectedRow != null) listOf(selectedRow) + filtered else filtered
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Coche un modèle ou « Aucun » pour une séance vide. Un seul choix à la fois.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CompactSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Rechercher un modèle",
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val noneSelected = selectedTemplateId == null
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTemplateId = null }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = noneSelected,
                                onCheckedChange = null,
                            )
                            Column(Modifier.padding(start = 4.dp)) {
                                Text(
                                    "Aucun (séance vide)",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    "Démarrer sans programme prédéfini",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    if (filteredTemplateRows.isEmpty()) {
                        Text(
                            if (qNorm.isEmpty()) {
                                "Aucun modèle enregistré. Tu peux en créer depuis l’onglet Modèles."
                            } else {
                                "Aucun modèle ne correspond à ta recherche."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    } else {
                        filteredTemplateRows.forEach { t ->
                            val selected = selectedTemplateId == t.id
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTemplateId = t.id }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = null,
                                    )
                                    Column(Modifier.padding(start = 4.dp)) {
                                        Text(t.name, style = MaterialTheme.typography.titleSmall)
                                        if (!t.description.isNullOrBlank()) {
                                            Text(
                                                t.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            "${t.exerciseCount} exercice(s) dans le programme",
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
            TextButton(onClick = { onCreate(selectedTemplateId) }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}
