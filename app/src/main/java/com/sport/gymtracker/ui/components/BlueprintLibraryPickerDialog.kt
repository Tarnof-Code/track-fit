package com.sport.gymtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.data.local.ExerciseBlueprintEntity
import com.sport.gymtracker.domain.prescriptionSummaryShort
import java.util.Locale

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
                var filterQuery by remember { mutableStateOf("") }
                val qNorm = filterQuery.trim().lowercase(Locale.FRENCH)
                val filteredBlueprints = remember(exerciseBlueprints, qNorm) {
                    if (qNorm.isEmpty()) exerciseBlueprints
                    else {
                        exerciseBlueprints.filter { bp ->
                            bp.name.lowercase(Locale.FRENCH).contains(qNorm) ||
                                bp.notes.lowercase(Locale.FRENCH).contains(qNorm) ||
                                bp.prescriptionSummaryShort().lowercase(Locale.FRENCH).contains(qNorm) ||
                                bp.equipment.lowercase(Locale.FRENCH).contains(qNorm)
                        }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Coche un ou plusieurs exercices, puis appuie sur « Ajouter ». L’ordre suit tes coches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    CompactOutlinedSearchField(
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        placeholderText = "Rechercher ...",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (filteredBlueprints.isEmpty()) {
                            Text(
                                "Aucun exercice ne correspond à ta recherche.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        } else {
                            filteredBlueprints.forEach { bp ->
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
                                            if (bp.notes.isNotBlank()) {
                                                Text(
                                                    bp.notes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactOutlinedSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors()
    val textStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Search,
        ),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                isError = false,
                placeholder = {
                    Text(
                        placeholderText,
                        style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                },
                colors = colors,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = colors,
                    )
                },
            )
        },
    )
}
