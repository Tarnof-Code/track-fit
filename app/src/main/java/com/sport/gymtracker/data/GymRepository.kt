package com.sport.gymtracker.data

import androidx.room.withTransaction
import com.sport.gymtracker.data.local.AppDatabase
import com.sport.gymtracker.data.local.ExerciseBlueprintEntity
import com.sport.gymtracker.data.local.ExerciseEntryEntity
import com.sport.gymtracker.data.local.ExercisePerformanceHistoryRow
import com.sport.gymtracker.data.local.TemplateExerciseEntity
import com.sport.gymtracker.data.local.WorkoutSessionEntity
import com.sport.gymtracker.data.local.WorkoutTemplateEntity
import com.sport.gymtracker.data.local.toTemplatePlacement
import com.sport.gymtracker.data.local.clearPerformanceSnapshot
import com.sport.gymtracker.data.local.withPerformanceSnapshotFromBlueprint
import com.sport.gymtracker.domain.Difficulty
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.SkillLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Ligne de modèle résolue avec la définition d’exercice unique. */
data class TemplateExerciseLine(
    val placement: TemplateExerciseEntity,
    val exercise: ExerciseBlueprintEntity,
)

/** Ligne de séance résolue avec la définition d’exercice unique. */
data class SessionExerciseLine(
    val entry: ExerciseEntryEntity,
    val exercise: ExerciseBlueprintEntity,
)

class GymRepository(private val db: AppDatabase) {

    private val sessionDao = db.workoutSessionDao()
    private val exerciseDao = db.exerciseEntryDao()
    private val templateDao = db.workoutTemplateDao()
    private val templateExerciseDao = db.templateExerciseDao()
    private val exerciseBlueprintDao = db.exerciseBlueprintDao()

    fun observeSessions() = sessionDao.observeAll()

    fun observeSession(sessionId: Long) = sessionDao.observeById(sessionId)

    fun observeSessionExerciseLines(sessionId: Long): Flow<List<SessionExerciseLine>> =
        combine(
            exerciseDao.observeForSession(sessionId),
            exerciseBlueprintDao.observeAll(),
        ) { entries, defs ->
            val byId = defs.associateBy { it.id }
            entries.mapNotNull { e ->
                val def = byId[e.exerciseId] ?: return@mapNotNull null
                SessionExerciseLine(e, def)
            }
        }

    fun observeTemplateListRows() = templateDao.observeListRows()

    fun observeTemplate(templateId: Long) = templateDao.observeById(templateId)

    fun observeTemplateExerciseLines(templateId: Long): Flow<List<TemplateExerciseLine>> =
        combine(
            templateExerciseDao.observeForTemplate(templateId),
            exerciseBlueprintDao.observeAll(),
        ) { placements, defs ->
            val byId = defs.associateBy { it.id }
            placements.mapNotNull { p ->
                val def = byId[p.exerciseId] ?: return@mapNotNull null
                TemplateExerciseLine(p, def)
            }
        }

    fun observeExerciseBlueprints() = exerciseBlueprintDao.observeAll()

    fun observeHomeState(): Flow<HomeState> =
        sessionDao.observeAll().flatMapLatest { sessions ->
            flow { emit(buildHomeState(sessions)) }
        }.flowOn(Dispatchers.Default)

    fun observeStatistics(): Flow<StatisticsOverview> =
        sessionDao.observeAll().flatMapLatest { sessions ->
            flow { emit(buildStatisticsOverview(sessions)) }
        }.flowOn(Dispatchers.Default)

    fun observeExerciseProgressList(): Flow<List<ExerciseProgressListItem>> =
        combine(
            exerciseDao.observeExerciseProgressSummaries(),
            exerciseBlueprintDao.observeAll(),
        ) { summaries, blueprints ->
            val byId = blueprints.associateBy { it.id }
            summaries.mapNotNull { row ->
                val def = byId[row.exerciseId] ?: return@mapNotNull null
                ExerciseProgressListItem(
                    blueprintId = def.id,
                    name = def.name,
                    pointCount = row.pointCount,
                )
            }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }.flowOn(Dispatchers.Default)

    fun observeExerciseBlueprint(blueprintId: Long) = exerciseBlueprintDao.observeById(blueprintId)

    fun observeExercisePerformanceHistory(blueprintId: Long): Flow<List<ExercisePerformanceHistoryRow>> =
        exerciseDao.observePerformanceHistoryForBlueprint(blueprintId)

    private suspend fun buildHomeState(
        sessions: List<WorkoutSessionEntity>,
    ): HomeState {
        val tz = TimeZone.getDefault()
        val now = System.currentTimeMillis()
        val weekStart = mondayStartOfWeekMillis(now, tz)
        val weekEndExclusive = weekStart + 7L * 24 * 60 * 60 * 1000

        val completed = sessions.filter { it.endTimeMillis != null }
        val thisWeek = completed.filter { it.startTimeMillis >= weekStart && it.startTimeMillis < weekEndExclusive }
        val lastCompleted = completed.maxByOrNull { it.startTimeMillis }
        val todayBucket = dayBucket(now)
        val lastDayBucket = lastCompleted?.let { dayBucket(it.startTimeMillis) }
        val dayMs = 24L * 60 * 60 * 1000
        val restDaysSinceLastSession = if (lastDayBucket != null) {
            ((todayBucket - lastDayBucket) / dayMs).toInt().coerceAtLeast(0)
        } else {
            0
        }

        val muscleCounts = mutableMapOf<String, Int>()
        for (s in thisWeek) {
            for (ex in exerciseDao.listForSession(s.id)) {
                val def = exerciseBlueprintDao.getById(ex.exerciseId) ?: continue
                MuscleGroup.fromStorageList(def.muscleGroupsCsv).forEach { m ->
                    muscleCounts[m.name] = (muscleCounts[m.name] ?: 0) + 1
                }
            }
        }

        return HomeState(
            sessionsThisWeek = thisWeek.size,
            lastCompletedSessionStartMillis = lastCompleted?.startTimeMillis,
            restDaysSinceLastSession = restDaysSinceLastSession,
            topMusclesThisWeek = muscleCounts.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key to it.value },
        )
    }

    private suspend fun buildStatisticsOverview(sessions: List<WorkoutSessionEntity>): StatisticsOverview {
        val completed = sessions
            .filter { it.endTimeMillis != null }
            .sortedBy { it.startTimeMillis }
        if (completed.isEmpty()) {
            return StatisticsOverview.empty()
        }

        val tz = TimeZone.getDefault()
        val now = System.currentTimeMillis()
        val weekStart = mondayStartOfWeekMillis(now, tz)
        val weekEndExclusive = weekStart + 7L * 24 * 60 * 60 * 1000
        val rangeFmt = SimpleDateFormat("d MMM yyyy", Locale.FRENCH)
        val weekRangeLabel =
            "${rangeFmt.format(Date(weekStart))} – ${rangeFmt.format(Date(weekEndExclusive - 1))}"

        val inCurrentWeek = completed.filter { it.startTimeMillis >= weekStart && it.startTimeMillis < weekEndExclusive }
        val dayFmt = SimpleDateFormat("EEE d MMM", Locale.FRENCH)
        val weekSessions = inCurrentWeek.map { s ->
            WeekSessionInfo(
                dayLabel = dayFmt.format(Date(s.startTimeMillis)),
                templateOrTitle = templateLabelForSession(s),
            )
        }

        val firstStart = completed.minOf { it.startTimeMillis }
        val lastStart = completed.maxOf { it.startTimeMillis }
        val dayMs = 24L * 60 * 60 * 1000
        val spanDays = ((lastStart - firstStart) / dayMs).toInt() + 1
        val weeksForAvg = kotlin.math.max(1f, spanDays / 7f)
        val monthsForAvg = inclusiveMonthCount(firstStart, lastStart, tz).coerceAtLeast(1).toFloat()

        val n = completed.size
        val avgSessionsPerWeek = n / weeksForAvg
        val avgSessionsPerMonth = n / monthsForAvg

        var totalGymMs = 0L
        for (s in completed) {
            val end = s.endTimeMillis ?: continue
            totalGymMs += (end - s.startTimeMillis).coerceAtLeast(0L)
        }
        val totalMinutes = totalGymMs / 60_000f
        val avgGymMinutesPerWeek = totalMinutes / weeksForAvg
        val avgGymMinutesPerMonth = totalMinutes / monthsForAvg

        val muscleCounts = mutableMapOf<String, Int>()
        for (s in completed) {
            for (ex in exerciseDao.listForSession(s.id)) {
                val def = exerciseBlueprintDao.getById(ex.exerciseId) ?: continue
                MuscleGroup.fromStorageList(def.muscleGroupsCsv).forEach { m ->
                    muscleCounts[m.name] = (muscleCounts[m.name] ?: 0) + 1
                }
            }
        }
        val maxMuscle = muscleCounts.values.maxOrNull() ?: 0
        val muscleFrequencies = muscleCounts.entries
            .sortedByDescending { it.value }
            .map { (key, count) ->
                MuscleFrequencyItem(
                    muscleKey = key,
                    labelFr = MuscleGroup.entries.find { it.name == key }?.labelFr ?: key,
                    count = count,
                    fractionOfMax = if (maxMuscle > 0) count.toFloat() / maxMuscle else 0f,
                )
            }

        return StatisticsOverview(
            weekRangeLabel = weekRangeLabel,
            currentWeekSessionCount = inCurrentWeek.size,
            currentWeekSessions = weekSessions,
            avgSessionsPerWeek = avgSessionsPerWeek,
            avgSessionsPerMonth = avgSessionsPerMonth,
            avgGymMinutesPerWeek = avgGymMinutesPerWeek,
            avgGymMinutesPerMonth = avgGymMinutesPerMonth,
            muscleFrequencies = muscleFrequencies,
            hasData = true,
        )
    }

    private suspend fun templateLabelForSession(s: WorkoutSessionEntity): String {
        val fromTemplate = s.sourceTemplateId?.let { tid ->
            templateDao.getById(tid)?.name?.trim()?.takeIf { it.isNotEmpty() }
        }
        return fromTemplate ?: s.title.trim().ifEmpty { "Séance" }
    }

    private fun mondayStartOfWeekMillis(millis: Long, tz: TimeZone): Long {
        val cal = Calendar.getInstance(tz)
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.timeInMillis = millis
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun inclusiveMonthCount(firstMillis: Long, lastMillis: Long, tz: TimeZone): Int {
        val c1 = Calendar.getInstance(tz)
        c1.timeInMillis = firstMillis
        val c2 = Calendar.getInstance(tz)
        c2.timeInMillis = lastMillis
        val y1 = c1.get(Calendar.YEAR)
        val m1 = c1.get(Calendar.MONTH)
        val y2 = c2.get(Calendar.YEAR)
        val m2 = c2.get(Calendar.MONTH)
        return (y2 - y1) * 12 + (m2 - m1) + 1
    }

    suspend fun startSession(templateId: Long?): Long {
        val resolvedTitle = when {
            templateId != null -> {
                templateDao.getById(templateId)?.name?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "Séance"
            }
            else -> "Séance"
        }
        val sid = sessionDao.insert(
            WorkoutSessionEntity(
                startTimeMillis = System.currentTimeMillis(),
                endTimeMillis = null,
                title = resolvedTitle,
                sourceTemplateId = templateId,
            ),
        )
        if (templateId != null) {
            val lines = templateExerciseDao.listForTemplate(templateId)
            lines.forEach { line ->
                exerciseDao.insert(
                    ExerciseEntryEntity(
                        sessionId = sid,
                        orderIndex = line.orderIndex,
                        exerciseId = line.exerciseId,
                        difficulty = Difficulty.MODERATE.name,
                        level = SkillLevel.INTERMEDIATE.name,
                        doneInSession = false,
                    ),
                )
            }
        }
        return sid
    }

    suspend fun endSession(sessionId: Long) {
        val session = sessionDao.getById(sessionId) ?: return
        if (session.endTimeMillis != null) return
        val endMillis = System.currentTimeMillis()
        db.withTransaction {
            val entries = exerciseDao.listForSession(sessionId)
            for (e in entries) {
                if (!e.doneInSession) continue
                val bp = exerciseBlueprintDao.getById(e.exerciseId) ?: continue
                exerciseDao.update(e.withPerformanceSnapshotFromBlueprint(bp, endMillis))
            }
            sessionDao.update(session.copy(endTimeMillis = endMillis))
        }
    }

    suspend fun getSession(id: Long): WorkoutSessionEntity? = sessionDao.getById(id)

    suspend fun deleteSession(id: Long) {
        sessionDao.deleteById(id)
    }

    suspend fun addExercise(entry: ExerciseEntryEntity): Long = exerciseDao.insert(entry)

    suspend fun updateExercise(entry: ExerciseEntryEntity) {
        exerciseDao.update(entry)
    }

    /**
     * Bascule [ExerciseEntryEntity.doneInSession]. Si la séance est déjà terminée, enregistre ou efface
     * l’instantané de performance (courbes de progression), en utilisant l’heure de fin de séance.
     */
    suspend fun updateExerciseDoneInSession(entryId: Long, done: Boolean) {
        val entry = exerciseDao.getById(entryId) ?: return
        val session = sessionDao.getById(entry.sessionId) ?: return
        val endMillis = session.endTimeMillis
        if (endMillis == null) {
            exerciseDao.update(entry.copy(doneInSession = done))
            return
        }
        if (done) {
            val bp = exerciseBlueprintDao.getById(entry.exerciseId) ?: return
            exerciseDao.update(
                entry.copy(doneInSession = true).withPerformanceSnapshotFromBlueprint(bp, endMillis),
            )
        } else {
            exerciseDao.update(entry.copy(doneInSession = false).clearPerformanceSnapshot())
        }
    }

    suspend fun deleteExercise(id: Long) = exerciseDao.deleteById(id)

    suspend fun getExercise(id: Long): ExerciseEntryEntity? = exerciseDao.getById(id)

    suspend fun nextOrderIndex(sessionId: Long): Int {
        val list = exerciseDao.listForSession(sessionId)
        return (list.maxOfOrNull { it.orderIndex } ?: -1) + 1
    }

    suspend fun createTemplate(name: String, description: String?): Long =
        templateDao.insert(
            WorkoutTemplateEntity(
                name = name.trim().ifBlank { "Modèle" },
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                createdAtMillis = System.currentTimeMillis(),
            ),
        )

    suspend fun updateTemplate(template: WorkoutTemplateEntity) = templateDao.update(template)

    suspend fun deleteTemplate(id: Long) {
        templateDao.deleteById(id)
    }

    suspend fun getTemplate(id: Long) = templateDao.getById(id)

    suspend fun addTemplateExercise(entry: TemplateExerciseEntity): Long =
        templateExerciseDao.insert(entry)

    suspend fun deleteTemplateExercise(id: Long) = templateExerciseDao.deleteById(id)

    suspend fun getTemplateExercise(id: Long) = templateExerciseDao.getById(id)

    suspend fun nextTemplateExerciseOrder(templateId: Long): Int {
        val list = templateExerciseDao.listForTemplate(templateId)
        return (list.maxOfOrNull { it.orderIndex } ?: -1) + 1
    }

    suspend fun insertExerciseBlueprint(entry: ExerciseBlueprintEntity): Long =
        exerciseBlueprintDao.insert(entry)

    suspend fun getExerciseBlueprint(id: Long) = exerciseBlueprintDao.getById(id)

    suspend fun updateExerciseBlueprint(entry: ExerciseBlueprintEntity) {
        exerciseBlueprintDao.update(entry)
    }

    /**
     * Supprime la fiche exercice uniquement si aucun modèle ni aucune séance ne la référence encore.
     * @return true si la ligne a été supprimée.
     */
    suspend fun deleteExerciseBlueprint(id: Long): Boolean {
        if (templateExerciseDao.countByExerciseId(id) > 0) return false
        if (exerciseDao.countByExerciseId(id) > 0) return false
        exerciseBlueprintDao.deleteById(id)
        return true
    }

    suspend fun addExerciseFromBlueprintToTemplate(blueprintId: Long, templateId: Long) {
        val blueprint = exerciseBlueprintDao.getById(blueprintId) ?: return
        val order = nextTemplateExerciseOrder(templateId)
        templateExerciseDao.insert(blueprint.toTemplatePlacement(templateId, order))
    }

    /**
     * Ajoute un exercice existant en fin de séance. Si la séance a été lancée depuis un modèle
     * ([WorkoutSessionEntity.sourceTemplateId]), la même ligne est ajoutée au modèle.
     */
    suspend fun addExerciseFromBlueprintToSession(sessionId: Long, blueprintId: Long) {
        val session = sessionDao.getById(sessionId) ?: return
        exerciseBlueprintDao.getById(blueprintId) ?: return
        val order = nextOrderIndex(sessionId)
        exerciseDao.insert(
            ExerciseEntryEntity(
                sessionId = sessionId,
                orderIndex = order,
                exerciseId = blueprintId,
                difficulty = Difficulty.MODERATE.name,
                level = SkillLevel.INTERMEDIATE.name,
                doneInSession = false,
            ),
        )
        session.sourceTemplateId?.let { tid ->
            addExerciseFromBlueprintToTemplate(blueprintId, tid)
        }
    }

    private fun dayBucket(millis: Long): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

data class ExerciseProgressListItem(
    val blueprintId: Long,
    val name: String,
    val pointCount: Int,
)

data class HomeState(
    val sessionsThisWeek: Int,
    /** Début de la dernière séance terminée ; null si aucune. */
    val lastCompletedSessionStartMillis: Long?,
    /** Nombre de jours civils entre le jour de cette séance et aujourd’hui (0 = aujourd’hui). */
    val restDaysSinceLastSession: Int,
    val topMusclesThisWeek: List<Pair<String, Int>>,
)
