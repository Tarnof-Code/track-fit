package com.sport.gymtracker.domain

import java.text.Collator
import java.util.Locale

private val muscleFrenchCollator: Collator = Collator.getInstance(Locale.FRENCH)

enum class Difficulty(val labelFr: String) {
    VERY_LIGHT("Très léger"),
    LIGHT("Léger"),
    MODERATE("Modéré"),
    HARD("Intense"),
    VERY_HARD("Très intense");

    companion object {
        fun fromStorage(s: String): Difficulty =
            entries.find { it.name == s } ?: MODERATE
    }
}

enum class SkillLevel(val labelFr: String) {
    BEGINNER("Débutant"),
    INTERMEDIATE("Intermédiaire"),
    ADVANCED("Avancé"),
    EXPERT("Expert");

    companion object {
        fun fromStorage(s: String): SkillLevel =
            entries.find { it.name == s } ?: INTERMEDIATE
    }
}

enum class MuscleGroup(val labelFr: String) {
    DOS("Dos"),
    PECTORAUX("Pectoraux"),
    EPAULES("Épaules"),
    BICEPS("Biceps"),
    CARDIO("Cardio"),
    TRICEPS("Triceps"),
    ABDOS("Abdos"),
    FESSIERS("Fessiers"),
    QUADRICEPS("Quadriceps"),
    ISCHIO_JAMB("Ischio"),
    ADDUCTEURS("Adducteurs"),
    ABDUCTEURS("Abducteurs"),
    MOLLETS("Mollets"),
    TRAPEZE("Trapèzes"),
    AVANT_BRAS("Avant-bras"),
    HAUT_DU_CORPS("Haut du corps"),
    BAS_DU_CORPS("Bas du corps"),
    FULL_BODY("Full body");

    companion object {
        /** Ordre alphabétique sur le libellé français (écrans de sélection). */
        val sortedByLabelFr: List<MuscleGroup>
            get() = entries.sortedWith(compareBy(muscleFrenchCollator) { it.labelFr })

        fun fromStorageList(csv: String): List<MuscleGroup> =
            csv.split(",")
                .mapNotNull { raw ->
                    when (val key = raw.trim()) {
                        "CORE" -> ABDOS // anciennes données
                        else -> entries.find { it.name == key }
                    }
                }
    }
}

/** Même ordre alphabétique français que [MuscleGroup.sortedByLabelFr]. */
fun Iterable<MuscleGroup>.sortedByFrenchLabel(): List<MuscleGroup> =
    sortedWith(compareBy(muscleFrenchCollator) { it.labelFr })

fun List<MuscleGroup>.toCsv(): String = joinToString(",") { it.name }

/** Repos par défaut entre les séries (secondes), utilisé à la création et comme suggestion. */
const val DEFAULT_REST_BETWEEN_SETS_SECONDS: Int = 180

@Suppress("UNUSED_PARAMETER")
fun recommendedRestSeconds(difficulty: Difficulty): Int = DEFAULT_REST_BETWEEN_SETS_SECONDS
