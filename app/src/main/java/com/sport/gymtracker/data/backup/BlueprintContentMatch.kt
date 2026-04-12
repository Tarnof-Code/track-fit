package com.sport.gymtracker.data.backup

import com.sport.gymtracker.data.local.ExerciseBlueprintEntity

/**
 * Même prescription / métadonnées utiles à l’entraînement, sans [id] ni [ExerciseBlueprintEntity.createdAtMillis].
 */
fun ExerciseBlueprintEntity.sameBlueprintContentAs(other: ExerciseBlueprintEntity): Boolean =
    name.trim() == other.name.trim() &&
        notes.trim() == other.notes.trim() &&
        sets == other.sets &&
        repsPerSet == other.repsPerSet &&
        durationSecondsPerSet == other.durationSecondsPerSet &&
        durationMinutesPerSet == other.durationMinutesPerSet &&
        loadSpec == other.loadSpec &&
        loadKg == other.loadKg &&
        machineLevel == other.machineLevel &&
        rowResistance == other.rowResistance &&
        workMode == other.workMode &&
        equipment.trim() == other.equipment.trim() &&
        muscleGroupsCsv == other.muscleGroupsCsv &&
        restBetweenSetsSeconds == other.restBetweenSetsSeconds
