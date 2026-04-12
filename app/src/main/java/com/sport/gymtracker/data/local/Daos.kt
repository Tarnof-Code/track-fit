package com.sport.gymtracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMillis DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionEntity?

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE endTimeMillis IS NOT NULL")
    fun observeCompletedCount(): Flow<Int>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ExerciseEntryDao {
    @Insert
    suspend fun insert(entry: ExerciseEntryEntity): Long

    @Update
    suspend fun update(entry: ExerciseEntryEntity)

    @Query("DELETE FROM exercise_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId ORDER BY orderIndex ASC, id ASC")
    fun observeForSession(sessionId: Long): Flow<List<ExerciseEntryEntity>>

    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId ORDER BY orderIndex ASC, id ASC")
    suspend fun listForSession(sessionId: Long): List<ExerciseEntryEntity>

    @Query("SELECT * FROM exercise_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExerciseEntryEntity?

    @Query("SELECT COUNT(*) FROM exercise_entries WHERE exerciseId = :exerciseId")
    suspend fun countByExerciseId(exerciseId: Long): Int
}

@Dao
interface WorkoutTemplateDao {
    @Insert
    suspend fun insert(template: WorkoutTemplateEntity): Long

    @Update
    suspend fun update(template: WorkoutTemplateEntity)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM workout_templates ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<WorkoutTemplateEntity>>

    @Query(
        """
        SELECT t.id, t.name, t.description, t.createdAtMillis,
        COALESCE((SELECT COUNT(*) FROM template_exercises e WHERE e.templateId = t.id), 0) AS exerciseCount
        FROM workout_templates t
        ORDER BY t.createdAtMillis DESC
        """,
    )
    fun observeListRows(): Flow<List<WorkoutTemplateListRow>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutTemplateEntity?>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getById(id: Long): WorkoutTemplateEntity?
}

@Dao
interface TemplateExerciseDao {
    @Insert
    suspend fun insert(entry: TemplateExerciseEntity): Long

    @Update
    suspend fun update(entry: TemplateExerciseEntity)

    @Delete
    suspend fun delete(entry: TemplateExerciseEntity)

    @Query("DELETE FROM template_exercises WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC, id ASC")
    fun observeForTemplate(templateId: Long): Flow<List<TemplateExerciseEntity>>

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC, id ASC")
    suspend fun listForTemplate(templateId: Long): List<TemplateExerciseEntity>

    @Query("SELECT * FROM template_exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TemplateExerciseEntity?

    @Query("SELECT COUNT(*) FROM template_exercises WHERE exerciseId = :exerciseId")
    suspend fun countByExerciseId(exerciseId: Long): Int
}

@Dao
interface ExerciseBlueprintDao {
    @Insert
    suspend fun insert(entry: ExerciseBlueprintEntity): Long

    @Update
    suspend fun update(entry: ExerciseBlueprintEntity)

    @Query("DELETE FROM exercise_blueprints WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM exercise_blueprints WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExerciseBlueprintEntity?

    @Query("SELECT * FROM exercise_blueprints ORDER BY name COLLATE NOCASE ASC, id ASC")
    fun observeAll(): Flow<List<ExerciseBlueprintEntity>>
}
