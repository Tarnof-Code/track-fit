package com.sport.gymtracker.data.local

import com.sport.gymtracker.domain.ExerciseDurationTimeUnit
import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.parseSingleLoadKg
import com.sport.gymtracker.domain.toCsv

/**
 * État des champs texte / sélection pour les trois écrans d’édition d’exercice (séance, modèle, bibliothèque).
 */
data class ExerciseBlueprintEditorFormState(
    val name: String,
    val notes: String,
    val workMode: ExerciseWorkMode,
    /** Pour [ExerciseWorkMode.TIME_DURATION] : unité du champ durée (ignoré pour les autres modes). */
    val durationTimeUnit: ExerciseDurationTimeUnit,
    val sets: String,
    /** null = conserver la valeur par défaut de l’écran (ex. reps « 10 »). */
    val reps: String?,
    val durationSec: String?,
    val durationMin: String?,
    val rowResistance: String,
    val equipment: String,
    val loadSpecStr: String,
    val rest: String,
    val selectedMuscles: Set<MuscleGroup>,
)

/**
 * Mappe une fiche persistée vers l’état de formulaire (à appliquer dans un [LaunchedEffect]).
 */
fun ExerciseBlueprintEntity.toEditorFormState(): ExerciseBlueprintEditorFormState {
    val loadFromSpec = loadSpec?.trim()?.takeIf { it.isNotEmpty() }
    val loadStr = loadFromSpec
        ?: loadKg?.let { v ->
            if (v == v.toInt().toFloat()) v.toInt().toString() else "%.1f".format(v)
        }.orEmpty()
    val mode = ExerciseWorkMode.fromStorage(workMode)
    val durationTimeUnit = when (mode) {
        ExerciseWorkMode.TIME_DURATION -> when {
            durationSecondsPerSet != null -> ExerciseDurationTimeUnit.SECONDS
            durationMinutesPerSet != null -> ExerciseDurationTimeUnit.MINUTES
            else -> ExerciseDurationTimeUnit.SECONDS
        }
        else -> ExerciseDurationTimeUnit.SECONDS
    }
    return ExerciseBlueprintEditorFormState(
        name = name,
        notes = notes,
        workMode = mode,
        durationTimeUnit = durationTimeUnit,
        sets = sets.toString(),
        reps = repsPerSet?.toString(),
        durationSec = durationSecondsPerSet?.toString(),
        durationMin = durationMinutesPerSet?.toString(),
        rowResistance = rowResistance.orEmpty().ifBlank { machineLevel?.toString().orEmpty() },
        equipment = equipment,
        loadSpecStr = loadStr,
        rest = restBetweenSetsSeconds.toString(),
        selectedMuscles = MuscleGroup.fromStorageList(muscleGroupsCsv).toSet(),
    )
}

/**
 * Construit une entité à partir des paramètres d’éditeur (logique unique pour les ViewModels).
 */
fun exerciseBlueprintFromEditorInput(
    workMode: ExerciseWorkMode,
    name: String,
    notes: String,
    sets: Int,
    repsPerSet: Int?,
    durationSecondsPerSet: Int?,
    durationMinutesPerSet: Int?,
    loadSpec: String?,
    rowResistance: String?,
    equipment: String,
    muscles: List<MuscleGroup>,
    restSeconds: Int,
    blueprintId: Long,
    createdAtMillis: Long,
): ExerciseBlueprintEntity {
    val loadTrimmed = loadSpec?.trim()?.takeIf { it.isNotEmpty() }
    return ExerciseBlueprintEntity(
        id = blueprintId,
        name = name.trim(),
        notes = notes.trim(),
        sets = sets.coerceAtLeast(1),
        repsPerSet = when (workMode) {
            ExerciseWorkMode.REPS_LOAD -> repsPerSet
            else -> null
        },
        durationSecondsPerSet = when (workMode) {
            ExerciseWorkMode.TIME_DURATION -> durationSecondsPerSet
            else -> null
        },
        durationMinutesPerSet = when (workMode) {
            ExerciseWorkMode.TIME_DURATION -> durationMinutesPerSet
            else -> null
        },
        loadSpec = when (workMode) {
            ExerciseWorkMode.REPS_LOAD -> loadTrimmed
            ExerciseWorkMode.TIME_DURATION -> if (durationSecondsPerSet != null) loadTrimmed else null
            else -> null
        },
        loadKg = when (workMode) {
            ExerciseWorkMode.REPS_LOAD -> parseSingleLoadKg(loadTrimmed)
            ExerciseWorkMode.TIME_DURATION -> if (durationSecondsPerSet != null) parseSingleLoadKg(loadTrimmed) else null
            else -> null
        },
        machineLevel = null,
        rowResistance = when (workMode) {
            ExerciseWorkMode.REPS_LOAD,
            ExerciseWorkMode.TIME_DURATION,
            -> rowResistance?.trim()?.takeIf { it.isNotEmpty() }
        },
        workMode = workMode.storageKey,
        equipment = equipment.trim(),
        muscleGroupsCsv = muscles.toCsv(),
        restBetweenSetsSeconds = restSeconds.coerceAtLeast(0),
        createdAtMillis = createdAtMillis,
    )
}
