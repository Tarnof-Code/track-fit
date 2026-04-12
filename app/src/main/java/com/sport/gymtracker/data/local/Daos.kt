package com.sport.gymtracker.data.local

import androidx.room.ColumnInfo
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

    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMillis ASC, id ASC")
    suspend fun listAll(): List<WorkoutSessionEntity>

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun countAll(): Int
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

    @Query(
        """
        SELECT e.exerciseId AS exerciseId, COUNT(*) AS pointCount
        FROM exercise_entries e
        WHERE e.perfCapturedAtMillis IS NOT NULL
        GROUP BY e.exerciseId
        ORDER BY e.exerciseId ASC
        """,
    )
    fun observeExerciseProgressSummaries(): Flow<List<ExerciseProgressSummaryRow>>

    @Query(
        """
        SELECT s.startTimeMillis AS sessionStartMillis,
               e.perfCapturedAtMillis AS perfCapturedAtMillis,
               e.perfWorkMode AS perfWorkMode,
               e.perfSets AS perfSets,
               e.perfRepsPerSet AS perfRepsPerSet,
               e.perfDurationSecondsPerSet AS perfDurationSecondsPerSet,
               e.perfDurationMinutesPerSet AS perfDurationMinutesPerSet,
               e.perfLoadKg AS perfLoadKg,
               e.perfLoadSpec AS perfLoadSpec,
               e.perfRowResistance AS perfRowResistance
        FROM exercise_entries e
        INNER JOIN workout_sessions s ON s.id = e.sessionId
        WHERE e.exerciseId = :blueprintId
          AND e.perfCapturedAtMillis IS NOT NULL
          AND s.endTimeMillis IS NOT NULL
        ORDER BY s.startTimeMillis ASC, e.orderIndex ASC, e.id ASC
        """,
    )
    fun observePerformanceHistoryForBlueprint(blueprintId: Long): Flow<List<ExercisePerformanceHistoryRow>>

    @Query("SELECT * FROM exercise_entries ORDER BY sessionId ASC, orderIndex ASC, id ASC")
    suspend fun listAll(): List<ExerciseEntryEntity>
}

data class ExerciseProgressSummaryRow(
    val exerciseId: Long,
    @ColumnInfo(name = "pointCount") val pointCount: Int,
)

data class ExercisePerformanceHistoryRow(
    @ColumnInfo(name = "sessionStartMillis") val sessionStartMillis: Long,
    @ColumnInfo(name = "perfCapturedAtMillis") val perfCapturedAtMillis: Long,
    @ColumnInfo(name = "perfWorkMode") val perfWorkMode: String?,
    @ColumnInfo(name = "perfSets") val perfSets: Int?,
    @ColumnInfo(name = "perfRepsPerSet") val perfRepsPerSet: Int?,
    @ColumnInfo(name = "perfDurationSecondsPerSet") val perfDurationSecondsPerSet: Int?,
    @ColumnInfo(name = "perfDurationMinutesPerSet") val perfDurationMinutesPerSet: Int?,
    @ColumnInfo(name = "perfLoadKg") val perfLoadKg: Float?,
    @ColumnInfo(name = "perfLoadSpec") val perfLoadSpec: String?,
    @ColumnInfo(name = "perfRowResistance") val perfRowResistance: String?,
)

@Dao
interface WorkoutTemplateDao {
    @Insert
    suspend fun insert(template: WorkoutTemplateEntity): Long

    @Update
    suspend fun update(template: WorkoutTemplateEntity)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM workout_templates ORDER BY name COLLATE LOCALIZED ASC, id ASC")
    fun observeAll(): Flow<List<WorkoutTemplateEntity>>

    @Query(
        """
        SELECT t.id, t.name, t.description, t.createdAtMillis,
        COALESCE((SELECT COUNT(*) FROM template_exercises e WHERE e.templateId = t.id), 0) AS exerciseCount
        FROM workout_templates t
        ORDER BY t.name COLLATE LOCALIZED ASC, t.id ASC
        """,
    )
    fun observeListRows(): Flow<List<WorkoutTemplateListRow>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutTemplateEntity?>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getById(id: Long): WorkoutTemplateEntity?

    @Query("SELECT * FROM workout_templates ORDER BY name COLLATE LOCALIZED ASC, id ASC")
    suspend fun listAll(): List<WorkoutTemplateEntity>

    @Query("SELECT COUNT(*) FROM workout_templates")
    suspend fun countAll(): Int
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

    @Query("SELECT * FROM template_exercises ORDER BY templateId ASC, orderIndex ASC, id ASC")
    suspend fun listAll(): List<TemplateExerciseEntity>
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

    @Query("SELECT * FROM exercise_blueprints WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ExerciseBlueprintEntity?>

    @Query("SELECT * FROM exercise_blueprints ORDER BY name COLLATE NOCASE ASC, id ASC")
    fun observeAll(): Flow<List<ExerciseBlueprintEntity>>

    @Query("SELECT * FROM exercise_blueprints ORDER BY id ASC")
    suspend fun listAll(): List<ExerciseBlueprintEntity>

    @Query("SELECT COUNT(*) FROM exercise_blueprints")
    suspend fun countAll(): Int
}
