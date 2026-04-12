package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.sortedByFrenchLabel
import com.sport.gymtracker.domain.exerciseTypeLabelFr
import com.sport.gymtracker.domain.intensitySummary
import com.sport.gymtracker.domain.prescriptionSummaryShort
import com.sport.gymtracker.domain.showsRestOnCard
import com.sport.gymtracker.ui.components.AddExerciseDropdownFab
import com.sport.gymtracker.ui.components.BlueprintLibraryPickerDialog
import com.sport.gymtracker.ui.components.ExerciseCardInfoContent
import com.sport.gymtracker.ui.viewmodel.TemplateDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: Long,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onEditExercise: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: TemplateDetailViewModel = viewModel(
        key = "template_detail_$templateId",
        factory = TemplateDetailViewModel.Factory(app, templateId),
    )
    val template by vm.template.collectAsState()
    val exercises by vm.exercises.collectAsState()
    val exerciseBlueprints by vm.exerciseBlueprints.collectAsState()
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var templateActionsMenuExpanded by remember { mutableStateOf(false) }
    var showBlueprintPicker by remember { mutableStateOf(false) }
    var blueprintPickerSelection by remember { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(showBlueprintPicker) {
        if (showBlueprintPicker) {
            blueprintPickerSelection = emptyList()
        }
    }
    var editingMeta by remember { mutableStateOf(false) }
    var metaName by remember { mutableStateOf("") }
    var metaDesc by remember { mutableStateOf("") }
    var deleteExerciseId by remember { mutableStateOf<Long?>(null) }
    var confirmDeleteTemplate by remember { mutableStateOf(false) }

    LaunchedEffect(template?.id) {
        val t = template ?: return@LaunchedEffect
        metaName = t.name
        metaDesc = t.description.orEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = template?.name ?: "Modèle",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Box {
                            IconButton(
                                onClick = { templateActionsMenuExpanded = true },
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Actions du modèle",
                                )
                            }
                            DropdownMenu(
                                expanded = templateActionsMenuExpanded,
                                onDismissRequest = { templateActionsMenuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Renommer") },
                                    onClick = {
                                        templateActionsMenuExpanded = false
                                        editingMeta = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer") },
                                    onClick = {
                                        templateActionsMenuExpanded = false
                                        confirmDeleteTemplate = true
                                    },
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
        floatingActionButton = {
            AddExerciseDropdownFab(
                expanded = fabMenuExpanded,
                onExpandedChange = { fabMenuExpanded = it },
                onNewExercise = onAddExercise,
                onFromLibrary = { showBlueprintPicker = true },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                template?.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                }
                Text(
                    "${exercises.size} exercice(s) dans ce programme",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                if (exercises.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        ),
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "Programme vide pour l’instant",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "Crée un exercice ou choisis-en un dans ta bibliothèque.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(
                                onClick = onAddExercise,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Nouvel exercice")
                            }
                            OutlinedButton(
                                onClick = { showBlueprintPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Depuis la bibliothèque")
                            }
                        }
                    }
                }
            }
            items(exercises, key = { it.placement.id }) { line ->
                val def = line.exercise
                val muscles = MuscleGroup.fromStorageList(def.muscleGroupsCsv)
                    .sortedByFrenchLabel()
                    .joinToString { it.labelFr }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        ExerciseCardInfoContent(
                            name = def.name,
                            notes = def.notes,
                            exerciseTypeLabel = def.exerciseTypeLabelFr(),
                            prescriptionLine = def.prescriptionSummaryShort(),
                            intensityLine = def.intensitySummary(),
                            equipment = def.equipment,
                            musclesLine = muscles,
                            showRestBetweenSets = def.showsRestOnCard(),
                            restBetweenSetsSeconds = def.restBetweenSetsSeconds,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Text(
                                text = "Modifier",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onEditExercise(line.placement.id) },
                            )
                            Text(
                                text = "Retirer du modèle",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { deleteExerciseId = line.placement.id },
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingMeta) {
        AlertDialog(
            onDismissRequest = { editingMeta = false },
            title = { Text("Modifier le modèle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = metaName,
                        onValueChange = { metaName = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = metaDesc,
                        onValueChange = { metaDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.saveMeta(metaName, metaDesc.takeIf { it.isNotBlank() })
                        editingMeta = false
                    },
                ) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { editingMeta = false }) { Text("Annuler") }
            },
        )
    }

    if (confirmDeleteTemplate) {
        AlertDialog(
            onDismissRequest = { confirmDeleteTemplate = false },
            title = { Text("Supprimer ce modèle ?") },
            text = { Text("Les exercices du modèle seront supprimés. Les séances déjà enregistrées ne sont pas affectées.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteTemplate { onBack() }
                        confirmDeleteTemplate = false
                    },
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTemplate = false }) { Text("Annuler") }
            },
        )
    }

    val delEx = deleteExerciseId
    if (delEx != null) {
        AlertDialog(
            onDismissRequest = { deleteExerciseId = null },
            title = { Text("Retirer cet exercice du modèle ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteExercise(delEx)
                        deleteExerciseId = null
                    },
                ) { Text("Retirer") }
            },
            dismissButton = {
                TextButton(onClick = { deleteExerciseId = null }) { Text("Annuler") }
            },
        )
    }

    if (showBlueprintPicker) {
        BlueprintLibraryPickerDialog(
            exerciseBlueprints = exerciseBlueprints,
            emptyLibraryMessage =
                "Aucun exercice réutilisable pour l’instant. Crée un exercice dans un modèle ou ajoute-en un à une séance : il sera ajouté automatiquement à la bibliothèque.",
            selectedIds = blueprintPickerSelection,
            onToggleBlueprintId = { id ->
                blueprintPickerSelection =
                    if (id in blueprintPickerSelection) {
                        blueprintPickerSelection.filter { it != id }
                    } else {
                        blueprintPickerSelection + id
                    }
            },
            onDismiss = { showBlueprintPicker = false },
            onConfirmAdd = {
                vm.addExercisesFromBlueprints(blueprintPickerSelection)
                showBlueprintPicker = false
            },
        )
    }
}
