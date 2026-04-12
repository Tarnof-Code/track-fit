package com.sport.gymtracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val title: String,
    /** Si non null, le nom de séance provient du modèle utilisé à la création. */
    val sourceTemplateId: Long? = null,
)

/** Exercice dans une séance : métadonnées de séance + référence à l’exercice unique ([exercise_blueprints]). */
@Entity(
    tableName = "exercise_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("exerciseId")],
)
data class ExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val orderIndex: Int,
    /** Identifiant dans [exercise_blueprints]. */
    val exerciseId: Long,
    val difficulty: String,
    val level: String,
    /** Exercice coché comme réalisé pendant la séance (séance en cours ou terminée). */
    val doneInSession: Boolean = false,
)
