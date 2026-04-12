package com.sport.gymtracker.data.backup

enum class DataImportMode {
    /** Ajoute le contenu du fichier aux données existantes (nouveaux identifiants). */
    MERGE,

    /** Efface toutes les données locales puis importe le fichier. */
    REPLACE,
}

/** Périmètre du contenu à importer depuis un fichier JSON. */
enum class ImportContentScope {
    /** Exercices (bibliothèque), modèles, séances et entrées. */
    ALL,

    /** Uniquement les fiches exercices ([exercise_blueprints]). */
    EXERCISES_ONLY,

    /**
     * Modèles et lignes de modèle, plus les fiches exercices référencées par ces modèles
     * (pas les séances).
     */
    TEMPLATES_AND_EXERCISES,
}

data class DataImportResult(
    val blueprints: Int,
    val templates: Int,
    val templateLines: Int,
    val sessions: Int,
    val sessionExercises: Int,
    /** Fiches déjà en bibliothèque (même contenu), réutilisées sans nouvelle ligne — import modèles. */
    val blueprintsReusedExisting: Int = 0,
)
