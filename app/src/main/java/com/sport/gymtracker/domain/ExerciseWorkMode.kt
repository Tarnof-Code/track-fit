package com.sport.gymtracker.domain

enum class ExerciseWorkMode(val storageKey: String, val labelFr: String) {
    REPS_LOAD("REPS_LOAD", "Reps (+ charge)"),
    TIME_SECONDS("TIME_SECONDS", "Durée (secondes) / nb séries"),
    TIME_MINUTES("TIME_MINUTES", "Durée (minutes)"),
    /** Durée en minutes par série + niveau ou réglage (escalier, rameur, etc.). */
    DURATION_AND_LEVEL("DURATION_AND_LEVEL", "Durée (minutes) et niveau"),
    ;

    companion object {
        fun fromStorage(key: String?): ExerciseWorkMode = when (key) {
            /** Anciennes valeurs (migration 6→7). */
            "STAIR_LEVEL", "ROW_RESISTANCE" -> DURATION_AND_LEVEL
            else -> entries.find { it.storageKey == key } ?: REPS_LOAD
        }
    }
}

/** Affiche le champ « Séries » dans l’éditeur (sinon une seule série est enregistrée en base). */
fun ExerciseWorkMode.showsSetsInEditor(): Boolean =
    this == ExerciseWorkMode.REPS_LOAD || this == ExerciseWorkMode.TIME_SECONDS

/** Affiche le repos entre séries dans l’éditeur. */
fun ExerciseWorkMode.showsRestInEditor(): Boolean =
    this == ExerciseWorkMode.REPS_LOAD || this == ExerciseWorkMode.TIME_SECONDS
