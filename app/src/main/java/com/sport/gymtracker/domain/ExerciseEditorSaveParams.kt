package com.sport.gymtracker.domain

/**
 * Paramètres validés pour l’enregistrement d’un exercice depuis les champs de l’éditeur.
 * Évite la triple copie du même `when (workMode)` dans les écrans Compose.
 */
data class ExerciseEditorSaveParams(
    val workMode: ExerciseWorkMode,
    val name: String,
    val sets: Int,
    val repsPerSet: Int?,
    val durationSecondsPerSet: Int?,
    val durationMinutesPerSet: Int?,
    val loadSpec: String?,
    val rowResistance: String?,
    val equipment: String,
    val muscles: List<MuscleGroup>,
    val restSeconds: Int,
)

/**
 * Interprète les champs texte de l’éditeur ; retourne null si la saisie est invalide pour le mode choisi.
 */
fun parseExerciseEditorSaveParams(
    workMode: ExerciseWorkMode,
    name: String,
    sets: String,
    reps: String,
    durationSec: String,
    durationMin: String,
    rowResistance: String,
    loadSpecStr: String,
    rest: String,
    equipment: String,
    muscles: List<MuscleGroup>,
    restFallback: Int,
): ExerciseEditorSaveParams? {
    if (name.isBlank()) return null
    val s = when (workMode) {
        ExerciseWorkMode.REPS_LOAD,
        ExerciseWorkMode.TIME_SECONDS,
        -> sets.toIntOrNull()?.coerceAtLeast(1) ?: return null
        else -> 1
    }
    val loadSpec = loadSpecStr.trim().takeIf { it.isNotEmpty() }
    val rr = rowResistance.trim().takeIf { it.isNotEmpty() }
    val restSec =
        if (workMode.showsRestInEditor()) rest.toIntOrNull() ?: restFallback else 0
    return when (workMode) {
        ExerciseWorkMode.REPS_LOAD -> {
            val r = reps.toIntOrNull() ?: return null
            ExerciseEditorSaveParams(
                workMode = workMode,
                name = name,
                sets = s,
                repsPerSet = r,
                durationSecondsPerSet = null,
                durationMinutesPerSet = null,
                loadSpec = loadSpec,
                rowResistance = null,
                equipment = equipment,
                muscles = muscles,
                restSeconds = restSec,
            )
        }
        ExerciseWorkMode.TIME_SECONDS -> {
            val dSec = durationSec.toIntOrNull() ?: return null
            ExerciseEditorSaveParams(
                workMode = workMode,
                name = name,
                sets = s,
                repsPerSet = null,
                durationSecondsPerSet = dSec,
                durationMinutesPerSet = null,
                loadSpec = loadSpec,
                rowResistance = null,
                equipment = equipment,
                muscles = muscles,
                restSeconds = restSec,
            )
        }
        ExerciseWorkMode.TIME_MINUTES -> {
            val dMin = durationMin.toIntOrNull() ?: return null
            ExerciseEditorSaveParams(
                workMode = workMode,
                name = name,
                sets = s,
                repsPerSet = null,
                durationSecondsPerSet = null,
                durationMinutesPerSet = dMin,
                loadSpec = null,
                rowResistance = null,
                equipment = equipment,
                muscles = muscles,
                restSeconds = restSec,
            )
        }
        ExerciseWorkMode.DURATION_AND_LEVEL -> {
            val dMin = durationMin.toIntOrNull() ?: return null
            if (rr == null) return null
            ExerciseEditorSaveParams(
                workMode = workMode,
                name = name,
                sets = s,
                repsPerSet = null,
                durationSecondsPerSet = null,
                durationMinutesPerSet = dMin,
                loadSpec = null,
                rowResistance = rr,
                equipment = equipment,
                muscles = muscles,
                restSeconds = restSec,
            )
        }
    }
}
