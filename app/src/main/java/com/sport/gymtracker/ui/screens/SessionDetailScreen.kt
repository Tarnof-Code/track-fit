package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.sortedByFrenchLabel
import com.sport.gymtracker.domain.exerciseTypeLabelFr
import com.sport.gymtracker.domain.intensitySummary
import com.sport.gymtracker.domain.prescriptionSummaryShort
import com.sport.gymtracker.domain.showsRestOnCard
import com.sport.gymtracker.ui.components.ExerciseCardInfoContent
import com.sport.gymtracker.ui.viewmodel.SessionDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ExerciseCardDoneGreen = Color(0xFFE8F5E9)
private val ExerciseCardDoneIconGreen = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onBack: () -> Unit,
    onAddNewExercise: () -> Unit,
    onEditExercise: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: SessionDetailViewModel = viewModel(factory = SessionDetailViewModel.Factory(app, sessionId))
    val session by vm.session.collectAsState()
    val exercises by vm.exercises.collectAsState()
    val exerciseBlueprints by vm.exerciseBlueprints.collectAsState()
    var confirmEnd by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Long?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDeleteSession by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var showBlueprintPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.title ?: "Séance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Supprimer") },
                                onClick = {
                                    confirmDeleteSession = true
                                    menuExpanded = false
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (session != null) {
                Box {
                    ExtendedFloatingActionButton(
                        onClick = { fabMenuExpanded = true },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text("Exercice") },
                    )
                    DropdownMenu(
                        expanded = fabMenuExpanded,
                        onDismissRequest = { fabMenuExpanded = false },
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nouvel exercice") },
                            onClick = {
                                fabMenuExpanded = false
                                onAddNewExercise()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Depuis la bibliothèque") },
                            onClick = {
                                fabMenuExpanded = false
                                showBlueprintPicker = true
                            },
                        )
                    }
                }
            }
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
                session?.let { s ->
                    val fmt = SimpleDateFormat("EEEE dd/MM/yyyy 'à' HH:mm", Locale.FRENCH)
                    Text("Début : ${fmt.format(Date(s.startTimeMillis))}", style = MaterialTheme.typography.bodyMedium)
                    if (s.endTimeMillis != null) {
                        Text("Fin : ${fmt.format(Date(s.endTimeMillis!!))}", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        TextButton(onClick = { confirmEnd = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Terminer la séance")
                        }
                    }
                }
            }
            items(exercises, key = { it.entry.id }) { line ->
                val def = line.exercise
                val muscles = MuscleGroup.fromStorageList(def.muscleGroupsCsv)
                    .sortedByFrenchLabel()
                    .joinToString { it.labelFr }
                val sessionActive = session?.endTimeMillis == null
                val done = line.entry.doneInSession
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (done) {
                            ExerciseCardDoneGreen
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Box(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 52.dp,
                                    top = 10.dp,
                                    bottom = 10.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            ExerciseCardInfoContent(
                                name = def.name,
                                exerciseTypeLabel = def.exerciseTypeLabelFr(),
                                prescriptionLine = def.prescriptionSummaryShort(),
                                intensityLine = def.intensitySummary(),
                                equipment = def.equipment,
                                musclesLine = muscles,
                                showRestBetweenSets = def.showsRestOnCard(),
                                restBetweenSetsSeconds = def.restBetweenSetsSeconds,
                                useRestCountdown = sessionActive,
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
                                    modifier = Modifier.clickable { onEditExercise(line.entry.id) },
                                )
                                Text(
                                    text = "Supprimer",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { deleteTarget = line.entry.id },
                                )
                            }
                        }
                        IconButton(
                            onClick = { vm.setExerciseDone(line.entry.id, !line.entry.doneInSession) },
                            modifier = Modifier.align(Alignment.TopEnd),
                        ) {
                            Icon(
                                imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = if (done) {
                                    "Annuler la validation"
                                } else {
                                    "Valider l’exercice"
                                },
                                tint = if (done) {
                                    ExerciseCardDoneIconGreen
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            title = { Text("Terminer la séance ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.endSession()
                        confirmEnd = false
                    },
                ) { Text("Terminer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmEnd = false }) { Text("Annuler") }
            },
        )
    }

    val del = deleteTarget
    if (del != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Supprimer cet exercice ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteExercise(del)
                        deleteTarget = null
                    },
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Annuler") }
            },
        )
    }

    if (confirmDeleteSession) {
        AlertDialog(
            onDismissRequest = { confirmDeleteSession = false },
            title = { Text("Supprimer cette séance ?") },
            text = { Text("Tous les exercices de cette séance seront supprimés.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteSession { onBack() }
                        confirmDeleteSession = false
                    },
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteSession = false }) { Text("Annuler") }
            },
        )
    }

    if (showBlueprintPicker) {
        AlertDialog(
            onDismissRequest = { showBlueprintPicker = false },
            title = { Text("Bibliothèque d’exercices") },
            text = {
                if (exerciseBlueprints.isEmpty()) {
                    Text(
                        "Aucun exercice dans la bibliothèque pour l’instant. Crée-en un avec « Nouvel exercice ».",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        exerciseBlueprints.forEach { bp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.addExerciseFromBlueprint(bp.id)
                                        showBlueprintPicker = false
                                    },
                            ) {
                                Column(Modifier.padding(12.dp)) {
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
            },
            confirmButton = {
                TextButton(onClick = { showBlueprintPicker = false }) { Text("Fermer") }
            },
        )
    }
}
