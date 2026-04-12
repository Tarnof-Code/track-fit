package com.sport.gymtracker.data.local

/**
 * Modèle de séance + nombre d’exercices du programme (pour les listes).
 */
data class WorkoutTemplateListRow(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAtMillis: Long,
    val exerciseCount: Int,
)
