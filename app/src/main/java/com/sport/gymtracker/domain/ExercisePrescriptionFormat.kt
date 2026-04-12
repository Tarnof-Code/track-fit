package com.sport.gymtracker.domain

import com.sport.gymtracker.data.local.ExerciseBlueprintEntity

private fun loadSummaryText(loadSpec: String?, loadKg: Float?): String? {
    val spec = loadSpec?.trim()?.takeIf { it.isNotEmpty() }
    if (spec != null) return spec
    return loadKg?.let { v -> "%.1f kg".format(v) }
}

private fun levelOrReglage(rowResistance: String?, machineLevel: Int?): String? =
    rowResistance?.trim()?.takeIf { it.isNotEmpty() }
        ?: machineLevel?.toString()

fun ExerciseBlueprintEntity.prescriptionSummaryShort(): String {
    val mode = ExerciseWorkMode.fromStorage(workMode)
    return buildString {
        when (mode) {
            ExerciseWorkMode.REPS_LOAD -> {
                append("${sets} série(s)")
                repsPerSet?.let { append(" × $it reps") }
            }
            ExerciseWorkMode.TIME_SECONDS -> {
                append("${sets} série(s)")
                durationSecondsPerSet?.let { append(" × ${it}s") }
            }
            ExerciseWorkMode.TIME_MINUTES ->
                durationMinutesPerSet?.let { append("$it min") }
            ExerciseWorkMode.DURATION_AND_LEVEL ->
                durationMinutesPerSet?.let { append("$it min") }
        }
    }
}

fun ExerciseBlueprintEntity.intensitySummary(): String? {
    val mode = ExerciseWorkMode.fromStorage(workMode)
    return when (mode) {
        ExerciseWorkMode.REPS_LOAD, ExerciseWorkMode.TIME_SECONDS ->
            loadSummaryText(loadSpec, loadKg)?.let { "Charge : $it" }
        ExerciseWorkMode.TIME_MINUTES -> null
        ExerciseWorkMode.DURATION_AND_LEVEL ->
            levelOrReglage(rowResistance, machineLevel)?.let { "Niveau : $it" }
    }
}

fun ExerciseBlueprintEntity.exerciseTypeLabelFr(): String =
    ExerciseWorkMode.fromStorage(workMode).labelFr

/** Afficher « Repos entre séries » sur les cartes (reps + charge, durée en secondes / séries). */
fun ExerciseBlueprintEntity.showsRestOnCard(): Boolean =
    ExerciseWorkMode.fromStorage(workMode).showsRestInEditor()
