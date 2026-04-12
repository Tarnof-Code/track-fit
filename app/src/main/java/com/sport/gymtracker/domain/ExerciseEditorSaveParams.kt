package com.sport.gymtracker.domain

/**
 * Paramètres validés pour l’enregistrement d’un exercice depuis les champs de l’éditeur.
 * Évite la triple copie du même `when (workMode)` dans les écrans Compose.
 */
data class ExerciseEditorSaveParams(
    val workMode: ExerciseWorkMode,
    val name: String,
    val notes: String,
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

/** Résultat de la validation des champs avant enregistrement d’un exercice. */
sealed interface ExerciseEditorParseResult {
    data class Ok(val params: ExerciseEditorSaveParams) : ExerciseEditorParseResult
    data class Err(val message: String) : ExerciseEditorParseResult
}

/**
 * Interprète les champs texte de l’éditeur ; retourne [ExerciseEditorParseResult.Err] avec un message si la saisie est invalide.
 */
fun parseExerciseEditorSaveParams(
    workMode: ExerciseWorkMode,
    durationTimeUnit: ExerciseDurationTimeUnit,
    name: String,
    notes: String,
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
): ExerciseEditorParseResult {
    if (name.isBlank()) {
        return ExerciseEditorParseResult.Err("Indiquez le nom de l’exercice.")
    }
    val notesTrimmed = notes.trim()
    val s = when (workMode) {
        ExerciseWorkMode.REPS_LOAD -> sets.toIntOrNull()?.coerceAtLeast(1)
            ?: return ExerciseEditorParseResult.Err("Indiquez un nombre de séries valide (au moins 1).")
        ExerciseWorkMode.TIME_DURATION -> sets.trim().toIntOrNull()?.coerceAtLeast(1) ?: 1
    }
    val loadSpec = loadSpecStr.trim().takeIf { it.isNotEmpty() }
    val rr = rowResistance.trim().takeIf { it.isNotEmpty() }
    val restSec =
        if (workMode.showsRestInEditor()) rest.toIntOrNull() ?: restFallback else 0
    return when (workMode) {
        ExerciseWorkMode.REPS_LOAD -> {
            val r = reps.toIntOrNull()
                ?: return ExerciseEditorParseResult.Err("Indiquez un nombre de répétitions valide.")
            ExerciseEditorParseResult.Ok(
                ExerciseEditorSaveParams(
                    workMode = workMode,
                    name = name,
                    notes = notesTrimmed,
                    sets = s,
                    repsPerSet = r,
                    durationSecondsPerSet = null,
                    durationMinutesPerSet = null,
                    loadSpec = loadSpec,
                    rowResistance = rr,
                    equipment = equipment,
                    muscles = muscles,
                    restSeconds = restSec,
                ),
            )
        }
        ExerciseWorkMode.TIME_DURATION -> when (durationTimeUnit) {
            ExerciseDurationTimeUnit.SECONDS -> {
                val dSec = durationSec.toIntOrNull()
                    ?: return ExerciseEditorParseResult.Err("Indiquez une durée en secondes valide.")
                ExerciseEditorParseResult.Ok(
                    ExerciseEditorSaveParams(
                        workMode = workMode,
                        name = name,
                        notes = notesTrimmed,
                        sets = s,
                        repsPerSet = null,
                        durationSecondsPerSet = dSec,
                        durationMinutesPerSet = null,
                        loadSpec = loadSpec,
                        rowResistance = rr,
                        equipment = equipment,
                        muscles = muscles,
                        restSeconds = restSec,
                    ),
                )
            }
            ExerciseDurationTimeUnit.MINUTES -> {
                val dMin = durationMin.toIntOrNull()
                    ?: return ExerciseEditorParseResult.Err("Indiquez une durée en minutes valide.")
                ExerciseEditorParseResult.Ok(
                    ExerciseEditorSaveParams(
                        workMode = workMode,
                        name = name,
                        notes = notesTrimmed,
                        sets = s,
                        repsPerSet = null,
                        durationSecondsPerSet = null,
                        durationMinutesPerSet = dMin,
                        loadSpec = null,
                        rowResistance = rr,
                        equipment = equipment,
                        muscles = muscles,
                        restSeconds = restSec,
                    ),
                )
            }
        }
    }
}
