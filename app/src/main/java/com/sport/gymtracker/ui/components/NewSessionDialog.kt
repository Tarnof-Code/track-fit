package com.sport.gymtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.data.local.WorkoutTemplateListRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionDialog(
    templateRows: List<WorkoutTemplateListRow>,
    onDismiss: () -> Unit,
    onCreate: (templateId: Long?) -> Unit,
    title: String = "Nouvelle séance",
    confirmLabel: String = "Créer",
) {
    var templateMenu by remember { mutableStateOf(false) }
    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }
    var selectedTemplateLabel by remember { mutableStateOf("Aucun (séance vide)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = templateMenu,
                    onExpandedChange = { templateMenu = it },
                ) {
                    OutlinedTextField(
                        value = selectedTemplateLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Modèle (optionnel)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = templateMenu,
                        onDismissRequest = { templateMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Aucun (séance vide)") },
                            onClick = {
                                selectedTemplateId = null
                                selectedTemplateLabel = "Aucun (séance vide)"
                                templateMenu = false
                            },
                        )
                        templateRows.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(t.name)
                                        Text(
                                            "${t.exerciseCount} exercice(s) dans le programme",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    selectedTemplateId = t.id
                                    selectedTemplateLabel = t.name
                                    templateMenu = false
                                },
                            )
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
