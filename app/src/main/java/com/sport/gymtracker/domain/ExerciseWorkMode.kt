package com.sport.gymtracker.domain

/** Unité de la durée saisie pour le mode [ExerciseWorkMode.TIME_DURATION]. */
enum class ExerciseDurationTimeUnit {
    SECONDS,
    MINUTES,
}

enum class ExerciseWorkMode(val storageKey: String, val labelFr: String) {
    REPS_LOAD("REPS_LOAD", "Reps (+ charge)"),
    /** Secondes ou minutes ; nb de séries et réglage optionnels (1 série si nb vide). */
    TIME_DURATION("TIME_DURATION", "Durée"),
    ;

    companion object {
        fun fromStorage(key: String?): ExerciseWorkMode = when (key) {
            /** Anciennes valeurs : tout est affiché comme [TIME_DURATION]. */
            "STAIR_LEVEL",
            "ROW_RESISTANCE",
            "DURATION_AND_LEVEL",
            "TIME_SECONDS",
            "TIME_MINUTES",
            -> TIME_DURATION
            else -> entries.find { it.storageKey == key } ?: REPS_LOAD
        }
    }
}

/** Affiche le champ « Nombre de séries » au-dessus du bloc [when] (le mode Durée le place après la durée). */
fun ExerciseWorkMode.showsSetsInEditor(): Boolean =
    when (this) {
        ExerciseWorkMode.REPS_LOAD -> true
        ExerciseWorkMode.TIME_DURATION -> false
    }

/** Affiche le repos entre séries dans l’éditeur. */
fun ExerciseWorkMode.showsRestInEditor(): Boolean =
    when (this) {
        ExerciseWorkMode.REPS_LOAD -> true
        ExerciseWorkMode.TIME_DURATION -> true
    }
