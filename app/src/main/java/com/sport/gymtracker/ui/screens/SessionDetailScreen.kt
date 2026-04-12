package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.sport.gymtracker.ui.components.ExerciseCardInfoContent
import com.sport.gymtracker.ui.theme.exerciseDoneCheckIconTint
import com.sport.gymtracker.ui.theme.sessionCompletedCardColors
import com.sport.gymtracker.ui.theme.sessionInProgressCardBackground
import com.sport.gymtracker.ui.viewmodel.SessionDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showSaveAsTemplate by remember { mutableStateOf(false) }
    var confirmSaveAsTemplate by remember { mutableStateOf(false) }
    var templateNameDraft by remember { mutableStateOf("") }
    var templateDescDraft by remember { mutableStateOf("") }
    var templateNameFieldError by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Long?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDeleteSession by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var showBlueprintPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    var blueprintPickerSelection by remember { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(showBlueprintPicker) {
        if (showBlueprintPicker) {
            blueprintPickerSelection = emptyList()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        if (exercises.isEmpty()) {
                            Text(
                                "Ajoutez au moins un exercice pour pouvoir terminer la séance.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        Button(
                            onClick = { confirmEnd = true },
                            enabled = exercises.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Terminer la séance",
                                style = MaterialTheme.typography.titleMedium,
                            )
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
                val completedPair = sessionCompletedCardColors()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        if (done) {
                            CardDefaults.cardColors(
                                containerColor = completedPair.first,
                                contentColor = completedPair.second,
                            )
                        } else {
                            CardDefaults.cardColors(
                                containerColor = sessionInProgressCardBackground(),
                            )
                        },
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
                                    exerciseDoneCheckIconTint()
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
                        if (exercises.isEmpty()) {
                            confirmEnd = false
                        } else {
                            val offerSaveAsTemplate =
                                session?.sourceTemplateId == null && exercises.isNotEmpty()
                            templateNameDraft = ""
                            templateDescDraft = ""
                            templateNameFieldError = false
                            confirmSaveAsTemplate = false
                            vm.endSession()
                            confirmEnd = false
                            if (offerSaveAsTemplate) {
                                showSaveAsTemplate = true
                            }
                        }
                    },
                    enabled = exercises.isNotEmpty(),
                ) { Text("Terminer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmEnd = false }) { Text("Annuler") }
            },
        )
    }

    if (showSaveAsTemplate) {
        if (confirmSaveAsTemplate) {
            val nameTrimmed = templateNameDraft.trim()
            val descPreview = templateDescDraft.trim().takeIf { it.isNotEmpty() }
            AlertDialog(
                onDismissRequest = { confirmSaveAsTemplate = false },
                title = { Text("Confirmer l’enregistrement") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Un nouveau modèle sera créé à partir de cette séance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (nameTrimmed.isNotEmpty()) {
                            Text(
                                nameTrimmed,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (descPreview != null) {
                            Text(
                                descPreview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (templateNameDraft.trim().isEmpty()) {
                                snackbarScope.launch {
                                    snackbarHostState.showSnackbar("Le modèle doit avoir un titre")
                                }
                                return@TextButton
                            }
                            vm.saveSessionAsTemplate(
                                templateNameDraft.trim(),
                                templateDescDraft.takeIf { it.isNotBlank() },
                            ) {
                                showSaveAsTemplate = false
                                confirmSaveAsTemplate = false
                                templateNameFieldError = false
                                snackbarScope.launch {
                                    snackbarHostState.showSnackbar("Modèle créé")
                                }
                            }
                        },
                    ) { Text("Confirmer") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmSaveAsTemplate = false }) { Text("Retour") }
                },
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    showSaveAsTemplate = false
                    confirmSaveAsTemplate = false
                    templateNameFieldError = false
                },
                title = { Text("Enregistrer comme modèle ?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Tu pourras réutiliser ce programme pour une prochaine séance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = templateNameDraft,
                            onValueChange = {
                                templateNameDraft = it
                                templateNameFieldError = false
                            },
                            label = { Text("Nom du modèle") },
                            singleLine = true,
                            isError = templateNameFieldError,
                            supportingText = {
                                if (templateNameFieldError) {
                                    Text(
                                        "Le modèle doit avoir un titre",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = templateDescDraft,
                            onValueChange = { templateDescDraft = it },
                            label = { Text("Description (optionnel)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (templateNameDraft.trim().isEmpty()) {
                                templateNameFieldError = true
                            } else {
                                confirmSaveAsTemplate = true
                            }
                        },
                    ) { Text("Enregistrer") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSaveAsTemplate = false
                            confirmSaveAsTemplate = false
                            templateNameFieldError = false
                        },
                    ) { Text("Non merci") }
                },
            )
        }
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
                                val selected = bp.id in blueprintPickerSelection
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                blueprintPickerSelection =
                                                    if (selected) {
                                                        blueprintPickerSelection.filter { it != bp.id }
                                                    } else {
                                                        blueprintPickerSelection + bp.id
                                                    }
                                            }
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
                    onClick = {
                        vm.addExercisesFromBlueprints(blueprintPickerSelection)
                        showBlueprintPicker = false
                    },
                    enabled = blueprintPickerSelection.isNotEmpty(),
                ) {
                    val n = blueprintPickerSelection.size
                    Text(if (n > 1) "Ajouter ($n)" else "Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlueprintPicker = false }) { Text("Fermer") }
            },
        )
    }
}
