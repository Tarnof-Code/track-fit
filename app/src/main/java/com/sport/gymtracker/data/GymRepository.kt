package com.sport.gymtracker.data

import androidx.room.withTransaction
import com.sport.gymtracker.data.backup.DataImportMode
import com.sport.gymtracker.data.backup.DataImportResult
import com.sport.gymtracker.data.backup.GYM_DATA_JSON_FORMAT_VERSION
import com.sport.gymtracker.data.backup.GymDataFile
import com.sport.gymtracker.data.backup.ImportContentScope
import com.sport.gymtracker.data.backup.decodeGymDataJson
import com.sport.gymtracker.data.backup.encodeGymDataJson
import com.sport.gymtracker.data.backup.sameBlueprintContentAs
import com.sport.gymtracker.data.backup.toBlueprintJson
import com.sport.gymtracker.data.backup.toEntity
import com.sport.gymtracker.data.backup.toExerciseEntryJson
import com.sport.gymtracker.data.backup.toSessionJson
import com.sport.gymtracker.data.backup.toTemplateExerciseJson
import com.sport.gymtracker.data.backup.toTemplateJson
import com.sport.gymtracker.data.backup.validateGymDataForImport
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
import com.sport.gymtracker.util.FrenchDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Ordre alphabétique sur le nom de fiche (libellé utilisateur), cohérent avec le français. */
private val FrenchExerciseNameCollator: Collator = Collator.getInstance(Locale.FRENCH)

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

@OptIn(ExperimentalCoroutinesApi::class)
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
            }.sortedWith { a, b ->
                val byName = FrenchExerciseNameCollator.compare(a.exercise.name, b.exercise.name)
                if (byName != 0) byName else a.entry.id.compareTo(b.entry.id)
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
            }.sortedWith { a, b ->
                val byName = FrenchExerciseNameCollator.compare(a.exercise.name, b.exercise.name)
                if (byName != 0) byName else a.placement.id.compareTo(b.placement.id)
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
        val weekRangeLabel =
            "${FrenchDateTime.formatStatsRangeDay(weekStart)} – ${FrenchDateTime.formatStatsRangeDay(weekEndExclusive - 1)}"

        val inCurrentWeek = completed.filter { it.startTimeMillis >= weekStart && it.startTimeMillis < weekEndExclusive }
        val weekSessions = inCurrentWeek.map { s ->
            WeekSessionInfo(
                dayLabel = FrenchDateTime.formatStatsWeekdayShort(s.startTimeMillis),
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

    suspend fun startSession(templateId: Long?): StartSessionResult {
        if (sessionDao.countActiveSessions() > 0) {
            return StartSessionResult.ActiveSessionExists
        }
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
        return StartSessionResult.Created(sid)
    }

    suspend fun endSession(sessionId: Long) {
        val session = sessionDao.getById(sessionId) ?: return
        if (session.endTimeMillis != null) return
        val entries = exerciseDao.listForSession(sessionId)
        if (entries.isEmpty()) return
        if (entries.none { it.doneInSession }) return
        val endMillis = System.currentTimeMillis()
        db.withTransaction {
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

    /**
     * Crée un modèle reprenant l’ordre et les fiches exercices de la séance (mêmes [exerciseId]).
     */
    suspend fun saveSessionAsTemplate(sessionId: Long, name: String, description: String?): Long =
        db.withTransaction {
            val entries = exerciseDao.listForSession(sessionId).sortedWith(
                compareBy<ExerciseEntryEntity> { it.orderIndex }.thenBy { it.id },
            )
            require(entries.isNotEmpty()) { "Aucun exercice dans cette séance." }
            val templateId = templateDao.insert(
                WorkoutTemplateEntity(
                    name = name.trim().ifBlank { "Modèle" },
                    description = description?.trim()?.takeIf { it.isNotEmpty() },
                    createdAtMillis = System.currentTimeMillis(),
                ),
            )
            for (e in entries) {
                templateExerciseDao.insert(
                    TemplateExerciseEntity(
                        id = 0L,
                        templateId = templateId,
                        orderIndex = e.orderIndex,
                        exerciseId = e.exerciseId,
                    ),
                )
            }
            templateId
        }

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

    suspend fun addBlueprintsToSessionInOrder(sessionId: Long, blueprintIds: List<Long>) {
        for (id in blueprintIds) addExerciseFromBlueprintToSession(sessionId, id)
    }

    suspend fun addBlueprintsToTemplateInOrder(templateId: Long, blueprintIds: List<Long>) {
        for (id in blueprintIds) addExerciseFromBlueprintToTemplate(id, templateId)
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

    suspend fun hasAnyStoredData(): Boolean =
        withContext(Dispatchers.IO) {
            exerciseBlueprintDao.countAll() > 0 ||
                sessionDao.countAll() > 0 ||
                templateDao.countAll() > 0
        }

    suspend fun exportDataJson(): String =
        withContext(Dispatchers.IO) {
            val blueprints = exerciseBlueprintDao.listAll().map { it.toBlueprintJson() }
            val templates = templateDao.listAll().map { it.toTemplateJson() }
            val templateExercises = templateExerciseDao.listAll().map { it.toTemplateExerciseJson() }
            val sessions = sessionDao.listAll().map { it.toSessionJson() }
            val exerciseEntries = exerciseDao.listAll().map { it.toExerciseEntryJson() }
            encodeGymDataJson(
                GymDataFile(
                    formatVersion = GYM_DATA_JSON_FORMAT_VERSION,
                    exportedAtMillis = System.currentTimeMillis(),
                    blueprints = blueprints,
                    templates = templates,
                    templateExercises = templateExercises,
                    sessions = sessions,
                    exerciseEntries = exerciseEntries,
                ),
            )
        }

    suspend fun importDataJson(
        json: String,
        mode: DataImportMode,
        scope: ImportContentScope = ImportContentScope.ALL,
    ): Result<DataImportResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = decodeGymDataJson(json)
                if (data.formatVersion > GYM_DATA_JSON_FORMAT_VERSION) {
                    error(
                        "Fichier trop récent (version ${data.formatVersion}). Mettez à jour l’application.",
                    )
                }
                validateGymDataForImport(data, scope)
                when (scope) {
                    ImportContentScope.ALL -> importAllFromFile(data, mode)
                    ImportContentScope.EXERCISES_ONLY -> importExercisesOnlyFromFile(data, mode)
                    ImportContentScope.TEMPLATES_AND_EXERCISES -> importTemplatesSliceFromFile(data, mode)
                }
            }
        }

    private suspend fun importAllFromFile(data: GymDataFile, mode: DataImportMode): DataImportResult =
        db.withTransaction {
            if (mode == DataImportMode.REPLACE) {
                db.clearAllTables()
            }
            val blueprintMap = LinkedHashMap<Long, Long>()
            for (b in data.blueprints) {
                val newId = exerciseBlueprintDao.insert(b.toEntity())
                blueprintMap[b.id] = newId
            }
            val templateMap = LinkedHashMap<Long, Long>()
            for (t in data.templates) {
                val newId = templateDao.insert(
                    WorkoutTemplateEntity(
                        id = 0L,
                        name = t.name,
                        description = t.description,
                        createdAtMillis = t.createdAtMillis,
                    ),
                )
                templateMap[t.id] = newId
            }
            for (te in data.templateExercises) {
                templateExerciseDao.insert(
                    TemplateExerciseEntity(
                        id = 0L,
                        templateId = templateMap.getValue(te.templateId),
                        orderIndex = te.orderIndex,
                        exerciseId = blueprintMap.getValue(te.exerciseId),
                    ),
                )
            }
            val sessionMap = LinkedHashMap<Long, Long>()
            for (s in data.sessions) {
                val newId = sessionDao.insert(
                    WorkoutSessionEntity(
                        id = 0L,
                        startTimeMillis = s.startTimeMillis,
                        endTimeMillis = s.endTimeMillis,
                        title = s.title,
                        sourceTemplateId = s.sourceTemplateId?.let { templateMap.getValue(it) },
                    ),
                )
                sessionMap[s.id] = newId
            }
            for (e in data.exerciseEntries) {
                exerciseDao.insert(
                    e.toEntity(
                        sessionId = sessionMap.getValue(e.sessionId),
                        exerciseId = blueprintMap.getValue(e.exerciseId),
                    ),
                )
            }
            DataImportResult(
                blueprints = data.blueprints.size,
                templates = data.templates.size,
                templateLines = data.templateExercises.size,
                sessions = data.sessions.size,
                sessionExercises = data.exerciseEntries.size,
                blueprintsReusedExisting = 0,
            )
        }

    private suspend fun importExercisesOnlyFromFile(data: GymDataFile, mode: DataImportMode): DataImportResult =
        db.withTransaction {
            if (mode == DataImportMode.REPLACE) {
                db.clearAllTables()
            }
            for (b in data.blueprints) {
                exerciseBlueprintDao.insert(b.toEntity())
            }
            DataImportResult(
                blueprints = data.blueprints.size,
                templates = 0,
                templateLines = 0,
                sessions = 0,
                sessionExercises = 0,
                blueprintsReusedExisting = 0,
            )
        }

    private suspend fun importTemplatesSliceFromFile(data: GymDataFile, mode: DataImportMode): DataImportResult {
        val neededBlueprintIds = data.templateExercises.map { it.exerciseId }.toSet()
        val blueprintsToImport = data.blueprints.filter { it.id in neededBlueprintIds }
        return db.withTransaction {
            if (mode == DataImportMode.REPLACE) {
                db.clearAllTables()
            }
            val blueprintPool = exerciseBlueprintDao.listAll().toMutableList()
            val blueprintMap = LinkedHashMap<Long, Long>()
            var reusedCount = 0
            for (b in blueprintsToImport) {
                val candidate = b.toEntity()
                val match = blueprintPool.find { it.sameBlueprintContentAs(candidate) }
                if (match != null) {
                    blueprintMap[b.id] = match.id
                    reusedCount++
                } else {
                    val newId = exerciseBlueprintDao.insert(candidate)
                    val inserted = exerciseBlueprintDao.getById(newId)!!
                    blueprintPool.add(inserted)
                    blueprintMap[b.id] = newId
                }
            }
            val templateMap = LinkedHashMap<Long, Long>()
            for (t in data.templates) {
                val newId = templateDao.insert(
                    WorkoutTemplateEntity(
                        id = 0L,
                        name = t.name,
                        description = t.description,
                        createdAtMillis = t.createdAtMillis,
                    ),
                )
                templateMap[t.id] = newId
            }
            for (te in data.templateExercises) {
                templateExerciseDao.insert(
                    TemplateExerciseEntity(
                        id = 0L,
                        templateId = templateMap.getValue(te.templateId),
                        orderIndex = te.orderIndex,
                        exerciseId = blueprintMap.getValue(te.exerciseId),
                    ),
                )
            }
            DataImportResult(
                blueprints = blueprintsToImport.size,
                templates = data.templates.size,
                templateLines = data.templateExercises.size,
                sessions = 0,
                sessionExercises = 0,
                blueprintsReusedExisting = reusedCount,
            )
        }
    }

    suspend fun clearAllLocalData(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                db.withTransaction {
                    db.clearAllTables()
                }
            }
        }
}

sealed class StartSessionResult {
    data class Created(val sessionId: Long) : StartSessionResult()
    data object ActiveSessionExists : StartSessionResult()
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
