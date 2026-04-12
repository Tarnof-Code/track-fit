package com.sport.gymtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Entité unique d’un exercice (bibliothèque, modèles et séances y font référence par id). */
@Entity(tableName = "exercise_blueprints")
data class ExerciseBlueprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Notes personnelles optionnelles (consignes, variantes, etc.). Chaîne vide = aucune. */
    val notes: String = "",
    val sets: Int,
    val repsPerSet: Int?,
    val durationSecondsPerSet: Int?,
    val durationMinutesPerSet: Int?,
    val loadSpec: String?,
    val loadKg: Float?,
    val machineLevel: Int?,
    val rowResistance: String?,
    val workMode: String,
    val equipment: String,
    val muscleGroupsCsv: String,
    val restBetweenSetsSeconds: Int,
    val createdAtMillis: Long,
)

fun ExerciseBlueprintEntity.toTemplatePlacement(templateId: Long, orderIndex: Int): TemplateExerciseEntity =
    TemplateExerciseEntity(
        templateId = templateId,
        orderIndex = orderIndex,
        exerciseId = id,
    )
