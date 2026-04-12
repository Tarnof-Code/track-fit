package com.sport.gymtracker.data.local

import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.parseSingleLoadKg
import com.sport.gymtracker.domain.toCsv

/**
 * État des champs texte / sélection pour les trois écrans d’édition d’exercice (séance, modèle, bibliothèque).
 */
data class ExerciseBlueprintEditorFormState(
    val name: String,
    val workMode: ExerciseWorkMode,
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
    return ExerciseBlueprintEditorFormState(
        name = name,
        workMode = ExerciseWorkMode.fromStorage(workMode),
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
        sets = sets.coerceAtLeast(1),
        repsPerSet = when (workMode) {
            ExerciseWorkMode.REPS_LOAD -> repsPerSet
            else -> null
        },
        durationSecondsPerSet = when (workMode) {
            ExerciseWorkMode.TIME_SECONDS -> durationSecondsPerSet
            else -> null
        },
        durationMinutesPerSet = when (workMode) {
            ExerciseWorkMode.TIME_MINUTES,
            ExerciseWorkMode.DURATION_AND_LEVEL,
            -> durationMinutesPerSet
            else -> null
        },
        loadSpec = when (workMode) {
            ExerciseWorkMode.REPS_LOAD,
            ExerciseWorkMode.TIME_SECONDS,
            -> loadTrimmed
            else -> null
        },
        loadKg = when (workMode) {
            ExerciseWorkMode.REPS_LOAD,
            ExerciseWorkMode.TIME_SECONDS,
            -> parseSingleLoadKg(loadTrimmed)
            else -> null
        },
        machineLevel = null,
        rowResistance = when (workMode) {
            ExerciseWorkMode.DURATION_AND_LEVEL ->
                rowResistance?.trim()?.takeIf { it.isNotEmpty() }
            else -> null
        },
        workMode = workMode.storageKey,
        equipment = equipment.trim(),
        muscleGroupsCsv = muscles.toCsv(),
        restBetweenSetsSeconds = restSeconds.coerceAtLeast(0),
        createdAtMillis = createdAtMillis,
    )
}
