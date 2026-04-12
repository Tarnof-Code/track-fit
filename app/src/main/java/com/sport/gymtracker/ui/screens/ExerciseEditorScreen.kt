package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.domain.DEFAULT_REST_BETWEEN_SETS_SECONDS
import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.showsRestInEditor
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.requireGymRepository
import com.sport.gymtracker.ui.components.CatalogEquipmentSelector
import com.sport.gymtracker.ui.components.ExercisePrescriptionSection
import com.sport.gymtracker.ui.viewmodel.ExerciseEditorViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseEditorScreen(
    sessionId: Long,
    exerciseId: Long?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: ExerciseEditorViewModel = viewModel(
        factory = ExerciseEditorViewModel.factory(app, sessionId, exerciseId),
    )

    var name by remember { mutableStateOf("") }
    var workMode by remember { mutableStateOf(ExerciseWorkMode.REPS_LOAD) }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var durationSec by remember { mutableStateOf("") }
    var durationMin by remember { mutableStateOf("") }
    var rowResistance by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var loadSpecStr by remember { mutableStateOf("") }
    var rest by remember { mutableStateOf(DEFAULT_REST_BETWEEN_SETS_SECONDS.toString()) }
    var selectedMuscles by remember { mutableStateOf(setOf<MuscleGroup>()) }

    LaunchedEffect(exerciseId) {
        val entryId = exerciseId ?: return@LaunchedEffect
        val entry = app.requireGymRepository().getExercise(entryId) ?: return@LaunchedEffect
        val ex = app.requireGymRepository().getExerciseBlueprint(entry.exerciseId) ?: return@LaunchedEffect
        name = ex.name
        workMode = ExerciseWorkMode.fromStorage(ex.workMode)
        sets = ex.sets.toString()
        ex.repsPerSet?.let { reps = it.toString() }
        ex.durationSecondsPerSet?.let { durationSec = it.toString() }
        ex.durationMinutesPerSet?.let { durationMin = it.toString() }
        rowResistance = ex.rowResistance.orEmpty().ifBlank { ex.machineLevel?.toString().orEmpty() }
        equipment = ex.equipment
        loadSpecStr = ex.loadSpec?.trim()?.takeIf { it.isNotEmpty() }
            ?: ex.loadKg?.let { v ->
                if (v == v.toInt().toFloat()) v.toInt().toString() else "%.1f".format(v)
            }.orEmpty()
        rest = ex.restBetweenSetsSeconds.toString()
        selectedMuscles = MuscleGroup.fromStorageList(ex.muscleGroupsCsv).toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (exerciseId == null) "Nouvel exercice" else "Modifier") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom de l’exercice") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            ExercisePrescriptionSection(
                workMode = workMode,
                onWorkModeChange = { workMode = it },
                sets = sets,
                onSetsChange = { sets = it },
                reps = reps,
                onRepsChange = { reps = it },
                durationSec = durationSec,
                onDurationSecChange = { durationSec = it },
                durationMin = durationMin,
                onDurationMinChange = { durationMin = it },
                levelOrReglage = rowResistance,
                onLevelOrReglageChange = { rowResistance = it },
                loadSpecStr = loadSpecStr,
                onLoadSpecChange = { loadSpecStr = it },
                chargeLabel = "Charge",
            )

            CatalogEquipmentSelector(
                equipmentValue = equipment,
                onEquipmentValueChange = { equipment = it },
                modifier = Modifier.fillMaxWidth(),
            )

            if (workMode.showsRestInEditor()) {
                OutlinedTextField(
                    value = rest,
                    onValueChange = { rest = it.filter { c -> c.isDigit() } },
                    label = { Text("Repos entre séries (s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text(
                    "Suggestion par défaut : $DEFAULT_REST_BETWEEN_SETS_SECONDS s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text("Muscles travaillés", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MuscleGroup.sortedByLabelFr.forEach { m ->
                    val sel = m in selectedMuscles
                    FilterChip(
                        selected = sel,
                        onClick = {
                            selectedMuscles = if (sel) selectedMuscles - m else selectedMuscles + m
                        },
                        label = { Text(m.labelFr) },
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val s = when (workMode) {
                        ExerciseWorkMode.REPS_LOAD,
                        ExerciseWorkMode.TIME_SECONDS,
                        -> sets.toIntOrNull()?.coerceAtLeast(1) ?: return@Button
                        else -> 1
                    }
                    val r = reps.toIntOrNull()
                    val dSec = durationSec.toIntOrNull()
                    val dMin = durationMin.toIntOrNull()
                    val rr = rowResistance.trim().takeIf { it.isNotEmpty() }
                    val loadSpec = loadSpecStr.trim().takeIf { it.isNotEmpty() }
                    val restSec = if (workMode.showsRestInEditor()) {
                        rest.toIntOrNull() ?: DEFAULT_REST_BETWEEN_SETS_SECONDS
                    } else {
                        0
                    }
                    when (workMode) {
                        ExerciseWorkMode.REPS_LOAD -> {
                            if (r == null) return@Button
                            vm.save(
                                workMode = workMode,
                                name = name,
                                sets = s,
                                repsPerSet = r,
                                durationSecondsPerSet = null,
                                durationMinutesPerSet = null,
                                loadSpec = loadSpec,
                                rowResistance = null,
                                equipment = equipment,
                                muscles = selectedMuscles.toList(),
                                restSeconds = restSec,
                                onDone = onBack,
                            )
                        }
                        ExerciseWorkMode.TIME_SECONDS -> {
                            if (dSec == null) return@Button
                            vm.save(
                                workMode = workMode,
                                name = name,
                                sets = s,
                                repsPerSet = null,
                                durationSecondsPerSet = dSec,
                                durationMinutesPerSet = null,
                                loadSpec = loadSpec,
                                rowResistance = null,
                                equipment = equipment,
                                muscles = selectedMuscles.toList(),
                                restSeconds = restSec,
                                onDone = onBack,
                            )
                        }
                        ExerciseWorkMode.TIME_MINUTES -> {
                            if (dMin == null) return@Button
                            vm.save(
                                workMode = workMode,
                                name = name,
                                sets = s,
                                repsPerSet = null,
                                durationSecondsPerSet = null,
                                durationMinutesPerSet = dMin,
                                loadSpec = null,
                                rowResistance = null,
                                equipment = equipment,
                                muscles = selectedMuscles.toList(),
                                restSeconds = restSec,
                                onDone = onBack,
                            )
                        }
                        ExerciseWorkMode.DURATION_AND_LEVEL -> {
                            if (rr == null || dMin == null) return@Button
                            vm.save(
                                workMode = workMode,
                                name = name,
                                sets = s,
                                repsPerSet = null,
                                durationSecondsPerSet = null,
                                durationMinutesPerSet = dMin,
                                loadSpec = null,
                                rowResistance = rr,
                                equipment = equipment,
                                muscles = selectedMuscles.toList(),
                                restSeconds = restSec,
                                onDone = onBack,
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Enregistrer")
            }
        }
    }
}
