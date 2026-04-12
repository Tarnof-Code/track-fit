package com.sport.gymtracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val createdAtMillis: Long,
)

/** Une ligne de programme : ordre dans le modèle + référence à l’exercice unique ([exercise_blueprints]). */
@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId"), Index("exerciseId")],
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val orderIndex: Int,
    /** Identifiant dans [exercise_blueprints] (entité unique partagée avec les séances). */
    val exerciseId: Long,
)
