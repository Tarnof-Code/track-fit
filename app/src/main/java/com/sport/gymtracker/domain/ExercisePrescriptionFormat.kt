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
            ExerciseWorkMode.TIME_DURATION -> when {
                durationSecondsPerSet != null -> {
                    append("${sets} série(s)")
                    append(" × ${durationSecondsPerSet}s")
                }
                durationMinutesPerSet != null -> {
                    append("${sets} série(s)")
                    append(" × ${durationMinutesPerSet} min")
                }
                else -> {}
            }
        }
    }
}

fun ExerciseBlueprintEntity.intensitySummary(): String? {
    val mode = ExerciseWorkMode.fromStorage(workMode)
    return when (mode) {
        ExerciseWorkMode.REPS_LOAD -> {
            val charge = loadSummaryText(loadSpec, loadKg)?.let { "Charge : $it" }
            val reglage = levelOrReglage(rowResistance, machineLevel)?.let { "Réglage : $it" }
            listOfNotNull(charge, reglage).joinToString(" · ").takeIf { it.isNotEmpty() }
        }
        ExerciseWorkMode.TIME_DURATION -> {
            val charge = if (durationSecondsPerSet != null) {
                loadSummaryText(loadSpec, loadKg)?.let { "Charge : $it" }
            } else {
                null
            }
            val reglage = levelOrReglage(rowResistance, machineLevel)?.let { "Réglage : $it" }
            listOfNotNull(charge, reglage).joinToString(" · ").takeIf { it.isNotEmpty() }
        }
    }
}

fun ExerciseBlueprintEntity.exerciseTypeLabelFr(): String =
    ExerciseWorkMode.fromStorage(workMode).labelFr

/** Afficher « Repos entre séries » sur les cartes (reps + charge, durée en secondes / séries). */
fun ExerciseBlueprintEntity.showsRestOnCard(): Boolean {
    val mode = ExerciseWorkMode.fromStorage(workMode)
    return when (mode) {
        ExerciseWorkMode.REPS_LOAD -> true
        ExerciseWorkMode.TIME_DURATION -> true
    }
}
