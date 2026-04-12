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
import com.sport.gymtracker.data.local.toEditorFormState
import com.sport.gymtracker.domain.Difficulty
import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.parseExerciseEditorSaveParams
import com.sport.gymtracker.domain.showsRestInEditor
import com.sport.gymtracker.requireGymRepository
import com.sport.gymtracker.ui.components.CatalogEquipmentSelector
import com.sport.gymtracker.ui.components.ExercisePrescriptionSection
import com.sport.gymtracker.ui.viewmodel.ExerciseBlueprintEditorViewModel
import com.sport.gymtracker.ui.viewmodel.defaultRestForDifficulty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseBlueprintEditorScreen(
    blueprintId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: ExerciseBlueprintEditorViewModel = viewModel(
        key = "blueprint_editor_$blueprintId",
        factory = ExerciseBlueprintEditorViewModel.factory(app, blueprintId),
    )

    val defaultRest = defaultRestForDifficulty(Difficulty.MODERATE)

    var name by remember { mutableStateOf("") }
    var workMode by remember { mutableStateOf(ExerciseWorkMode.REPS_LOAD) }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var durationSec by remember { mutableStateOf("") }
    var durationMin by remember { mutableStateOf("") }
    var rowResistance by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var loadSpecStr by remember { mutableStateOf("") }
    var rest by remember { mutableStateOf(defaultRest.toString()) }
    var selectedMuscles by remember { mutableStateOf(setOf<MuscleGroup>()) }

    LaunchedEffect(blueprintId) {
        if (blueprintId == 0L) return@LaunchedEffect
        val bp = app.requireGymRepository().getExerciseBlueprint(blueprintId) ?: return@LaunchedEffect
        val f = bp.toEditorFormState()
        name = f.name
        workMode = f.workMode
        sets = f.sets
        f.reps?.let { reps = it }
        f.durationSec?.let { durationSec = it }
        f.durationMin?.let { durationMin = it }
        rowResistance = f.rowResistance
        equipment = f.equipment
        loadSpecStr = f.loadSpecStr
        rest = f.rest
        selectedMuscles = f.selectedMuscles
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (blueprintId == 0L) "Nouvel exercice" else "Modifier l’exercice")
                },
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
            Text(
                if (blueprintId == 0L) {
                    "La fiche est enregistrée dans la bibliothèque. Tu pourras l’ajouter ensuite à un modèle ou à une séance."
                } else {
                    "Les changements s’appliquent partout où cet exercice est référencé : bibliothèque, modèles de séance et séances en cours."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

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
                chargeLabel = "Charge cible",
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
                    "Suggestion par défaut : $defaultRest s",
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
                    parseExerciseEditorSaveParams(
                        workMode = workMode,
                        name = name,
                        sets = sets,
                        reps = reps,
                        durationSec = durationSec,
                        durationMin = durationMin,
                        rowResistance = rowResistance,
                        loadSpecStr = loadSpecStr,
                        rest = rest,
                        equipment = equipment,
                        muscles = selectedMuscles.toList(),
                        restFallback = defaultRest,
                    )?.let { vm.save(it, onBack) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (blueprintId == 0L) "Créer l’exercice" else "Enregistrer")
            }
        }
    }
}
