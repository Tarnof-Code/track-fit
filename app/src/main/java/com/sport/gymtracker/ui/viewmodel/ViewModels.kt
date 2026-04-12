package com.sport.gymtracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sport.gymtracker.data.backup.DataImportMode
import com.sport.gymtracker.data.backup.DataImportResult
import com.sport.gymtracker.data.backup.ImportContentScope
import com.sport.gymtracker.data.ExerciseProgressListItem
import com.sport.gymtracker.data.GymRepository
import com.sport.gymtracker.data.HomeState
import com.sport.gymtracker.data.StatisticsOverview
import com.sport.gymtracker.data.local.ExerciseBlueprintEntity
import com.sport.gymtracker.data.local.ExerciseEntryEntity
import com.sport.gymtracker.data.local.ExercisePerformanceHistoryRow
import com.sport.gymtracker.data.local.TemplateExerciseEntity
import com.sport.gymtracker.domain.Difficulty
import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.SkillLevel
import com.sport.gymtracker.domain.parseSingleLoadKg
import com.sport.gymtracker.domain.recommendedRestSeconds
import com.sport.gymtracker.domain.toCsv
import com.sport.gymtracker.requireGymRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: GymRepository) : ViewModel() {
    val home: StateFlow<HomeState?> = repo.observeHomeState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun exportDataJson(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            onResult(runCatching { repo.exportDataJson() })
        }
    }

    fun importDataJson(
        json: String,
        mode: DataImportMode,
        scope: ImportContentScope = ImportContentScope.ALL,
        onResult: (Result<DataImportResult>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(repo.importDataJson(json, mode, scope))
        }
    }

    fun clearAllLocalData(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repo.clearAllLocalData())
        }
    }

    fun hasAnyStoredData(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repo.hasAnyStoredData())
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(app.requireGymRepository()) as T
        }
    }
}

class StatisticsViewModel(private val repo: GymRepository) : ViewModel() {
    val stats: StateFlow<StatisticsOverview> = repo.observeStatistics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsOverview.empty())

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StatisticsViewModel(app.requireGymRepository()) as T
        }
    }
}

data class ExerciseProgressDetailUi(
    val blueprintName: String,
    val workMode: ExerciseWorkMode,
    val history: List<ExercisePerformanceHistoryRow>,
)

class ExerciseProgressListViewModel(private val repo: GymRepository) : ViewModel() {
    val items: StateFlow<List<ExerciseProgressListItem>> =
        repo.observeExerciseProgressList()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExerciseProgressListViewModel(app.requireGymRepository()) as T
        }
    }
}

class ExerciseProgressDetailViewModel(
    private val repo: GymRepository,
    private val blueprintId: Long,
) : ViewModel() {
    val ui: StateFlow<ExerciseProgressDetailUi?> =
        combine(
            repo.observeExerciseBlueprint(blueprintId),
            repo.observeExercisePerformanceHistory(blueprintId),
        ) { bp, hist ->
            if (bp == null) return@combine null
            ExerciseProgressDetailUi(
                blueprintName = bp.name,
                workMode = ExerciseWorkMode.fromStorage(bp.workMode),
                history = hist,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    class Factory(
        private val app: Application,
        private val blueprintId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExerciseProgressDetailViewModel(app.requireGymRepository(), blueprintId) as T
    }
}

class SessionsViewModel(private val repo: GymRepository) : ViewModel() {
    val sessions = repo.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val templateRows = repo.observeTemplateListRows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun startSession(templateId: Long?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.startSession(templateId)
            onCreated(id)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repo.deleteSession(id) }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SessionsViewModel(app.requireGymRepository()) as T
        }
    }
}

class SessionDetailViewModel(
    private val repo: GymRepository,
    private val sessionId: Long,
) : ViewModel() {
    val session = repo.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val exercises = repo.observeSessionExerciseLines(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exerciseBlueprints = repo.observeExerciseBlueprints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addExerciseFromBlueprint(blueprintId: Long) {
        viewModelScope.launch {
            repo.addExerciseFromBlueprintToSession(sessionId, blueprintId)
        }
    }

    fun endSession() {
        viewModelScope.launch { repo.endSession(sessionId) }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            repo.deleteExercise(id)
        }
    }

    fun deleteSession(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteSession(sessionId)
            onDone()
        }
    }

    fun setExerciseDone(exerciseId: Long, done: Boolean) {
        viewModelScope.launch {
            val ex = repo.getExercise(exerciseId) ?: return@launch
            if (ex.sessionId != sessionId) return@launch
            repo.updateExerciseDoneInSession(exerciseId, done)
        }
    }

    class Factory(
        private val app: Application,
        private val sessionId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionDetailViewModel(app.requireGymRepository(), sessionId) as T
    }
}

class ExerciseEditorViewModel(
    private val repo: GymRepository,
    private val sessionId: Long,
    private val exerciseId: Long?,
) : ViewModel() {
    fun save(
        workMode: ExerciseWorkMode,
        name: String,
        sets: Int,
        repsPerSet: Int?,
        durationSecondsPerSet: Int?,
        durationMinutesPerSet: Int?,
        loadSpec: String?,
        rowResistance: String?,
        equipment: String,
        muscles: List<MuscleGroup>,
        restSeconds: Int,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val existing = exerciseId?.let { repo.getExercise(it) }
            val order = if (exerciseId != null) {
                existing?.orderIndex ?: repo.nextOrderIndex(sessionId)
            } else {
                repo.nextOrderIndex(sessionId)
            }
            val loadTrimmed = loadSpec?.trim()?.takeIf { it.isNotEmpty() }
            val bpExisting = existing?.let { repo.getExerciseBlueprint(it.exerciseId) }
            val blueprint = ExerciseBlueprintEntity(
                id = bpExisting?.id ?: 0L,
                name = name.trim(),
                sets = sets.coerceAtLeast(1),
                repsPerSet = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD -> repsPerSet
                    else -> null
                },
                durationSecondsPerSet = when (workMode) {
                    ExerciseWorkMode.TIME_SECONDS -> durationSecondsPerSet
                    else -> null
                },
                durationMinutesPerSet = when (workMode) {
                    ExerciseWorkMode.TIME_MINUTES,
                    ExerciseWorkMode.DURATION_AND_LEVEL,
                    -> durationMinutesPerSet
                    else -> null
                },
                loadSpec = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD,
                    ExerciseWorkMode.TIME_SECONDS,
                    -> loadTrimmed
                    else -> null
                },
                loadKg = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD,
                    ExerciseWorkMode.TIME_SECONDS,
                    -> parseSingleLoadKg(loadTrimmed)
                    else -> null
                },
                machineLevel = null,
                rowResistance = when (workMode) {
                    ExerciseWorkMode.DURATION_AND_LEVEL ->
                        rowResistance?.trim()?.takeIf { it.isNotEmpty() }
                    else -> null
                },
                workMode = workMode.storageKey,
                equipment = equipment.trim(),
                muscleGroupsCsv = muscles.toCsv(),
                restBetweenSetsSeconds = restSeconds.coerceAtLeast(0),
                createdAtMillis = bpExisting?.createdAtMillis ?: System.currentTimeMillis(),
            )
            if (exerciseId != null) {
                val entry = existing!!
                repo.updateExerciseBlueprint(
                    blueprint.copy(id = entry.exerciseId, createdAtMillis = bpExisting!!.createdAtMillis),
                )
                repo.updateExercise(entry.copy(orderIndex = order))
            } else {
                val newBpId = repo.insertExerciseBlueprint(blueprint.copy(id = 0L))
                repo.addExercise(
                    ExerciseEntryEntity(
                        sessionId = sessionId,
                        orderIndex = order,
                        exerciseId = newBpId,
                        difficulty = Difficulty.MODERATE.name,
                        level = SkillLevel.INTERMEDIATE.name,
                        doneInSession = false,
                    ),
                )
                repo.getSession(sessionId)?.sourceTemplateId?.let { tid ->
                    repo.addExerciseFromBlueprintToTemplate(newBpId, tid)
                }
            }
            onDone()
        }
    }

    companion object {
        fun factory(app: Application, sessionId: Long, exerciseId: Long?) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ExerciseEditorViewModel(app.requireGymRepository(), sessionId, exerciseId) as T
            }
    }
}

fun defaultRestForDifficulty(difficulty: Difficulty): Int =
    recommendedRestSeconds(difficulty)

class TemplatesListViewModel(private val repo: GymRepository) : ViewModel() {
    val templateRows = repo.observeTemplateListRows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createTemplate(name: String, description: String?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.createTemplate(name, description)
            onCreated(id)
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TemplatesListViewModel(app.requireGymRepository()) as T
        }
    }
}

class TemplateDetailViewModel(
    private val repo: GymRepository,
    private val templateId: Long,
) : ViewModel() {
    val template = repo.observeTemplate(templateId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val exercises = repo.observeTemplateExerciseLines(templateId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exerciseBlueprints = repo.observeExerciseBlueprints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveMeta(name: String, description: String?) {
        viewModelScope.launch {
            val t = repo.getTemplate(templateId) ?: return@launch
            repo.updateTemplate(
                t.copy(
                    name = name.trim().ifBlank { t.name },
                    description = description?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
        }
    }

    fun deleteTemplate(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteTemplate(templateId)
            onDone()
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch { repo.deleteTemplateExercise(id) }
    }

    fun addExerciseFromBlueprint(blueprintId: Long) {
        viewModelScope.launch {
            repo.addExerciseFromBlueprintToTemplate(blueprintId, templateId)
        }
    }

    class Factory(
        private val app: Application,
        private val templateId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TemplateDetailViewModel(app.requireGymRepository(), templateId) as T
    }
}

class ExerciseLibraryViewModel(private val repo: GymRepository) : ViewModel() {
    val blueprints = repo.observeExerciseBlueprints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteBlueprint(id: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            onDone(repo.deleteExerciseBlueprint(id))
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExerciseLibraryViewModel(app.requireGymRepository()) as T
        }
    }
}

class ExerciseBlueprintEditorViewModel(
    private val repo: GymRepository,
    private val blueprintId: Long,
) : ViewModel() {
    fun save(
        workMode: ExerciseWorkMode,
        name: String,
        sets: Int,
        repsPerSet: Int?,
        durationSecondsPerSet: Int?,
        durationMinutesPerSet: Int?,
        loadSpec: String?,
        rowResistance: String?,
        equipment: String,
        muscles: List<MuscleGroup>,
        restSeconds: Int,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val loadTrimmed = loadSpec?.trim()?.takeIf { it.isNotEmpty() }
            val now = System.currentTimeMillis()
            fun buildEntity(id: Long, createdAtMillis: Long) = ExerciseBlueprintEntity(
                id = id,
                name = name.trim(),
                sets = sets.coerceAtLeast(1),
                repsPerSet = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD -> repsPerSet
                    else -> null
                },
                durationSecondsPerSet = when (workMode) {
                    ExerciseWorkMode.TIME_SECONDS -> durationSecondsPerSet
                    else -> null
                },
                durationMinutesPerSet = when (workMode) {
                    ExerciseWorkMode.TIME_MINUTES,
                    ExerciseWorkMode.DURATION_AND_LEVEL,
                    -> durationMinutesPerSet
                    else -> null
                },
                loadSpec = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD,
                    ExerciseWorkMode.TIME_SECONDS,
                    -> loadTrimmed
                    else -> null
                },
                loadKg = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD,
                    ExerciseWorkMode.TIME_SECONDS,
                    -> parseSingleLoadKg(loadTrimmed)
                    else -> null
                },
                machineLevel = null,
                rowResistance = when (workMode) {
                    ExerciseWorkMode.DURATION_AND_LEVEL ->
                        rowResistance?.trim()?.takeIf { it.isNotEmpty() }
                    else -> null
                },
                workMode = workMode.storageKey,
                equipment = equipment.trim(),
                muscleGroupsCsv = muscles.toCsv(),
                restBetweenSetsSeconds = restSeconds.coerceAtLeast(0),
                createdAtMillis = createdAtMillis,
            )
            if (blueprintId == 0L) {
                repo.insertExerciseBlueprint(buildEntity(0L, now))
            } else {
                val existing = repo.getExerciseBlueprint(blueprintId) ?: return@launch
                repo.updateExerciseBlueprint(
                    buildEntity(blueprintId, existing.createdAtMillis),
                )
            }
            onDone()
        }
    }

    companion object {
        fun factory(app: Application, blueprintId: Long) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ExerciseBlueprintEditorViewModel(app.requireGymRepository(), blueprintId) as T
            }
    }
}

class TemplateExerciseEditorViewModel(
    private val repo: GymRepository,
    private val templateId: Long,
    private val exerciseId: Long?,
) : ViewModel() {
    fun save(
        workMode: ExerciseWorkMode,
        name: String,
        sets: Int,
        repsPerSet: Int?,
        durationSecondsPerSet: Int?,
        durationMinutesPerSet: Int?,
        loadSpec: String?,
        rowResistance: String?,
        equipment: String,
        muscles: List<MuscleGroup>,
        restSeconds: Int,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val existingPlacement = exerciseId?.let { repo.getTemplateExercise(it) }
            val order = existingPlacement?.orderIndex
                ?: repo.nextTemplateExerciseOrder(templateId)
            val bpExisting = existingPlacement?.let { repo.getExerciseBlueprint(it.exerciseId) }
            val loadTrimmed = loadSpec?.trim()?.takeIf { it.isNotEmpty() }
            val blueprint = ExerciseBlueprintEntity(
                id = bpExisting?.id ?: 0L,
                name = name.trim(),
                sets = sets.coerceAtLeast(1),
                repsPerSet = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD -> repsPerSet
                    else -> null
                },
                durationSecondsPerSet = when (workMode) {
                    ExerciseWorkMode.TIME_SECONDS -> durationSecondsPerSet
                    else -> null
                },
                durationMinutesPerSet = when (workMode) {
                    ExerciseWorkMode.TIME_MINUTES,
                    ExerciseWorkMode.DURATION_AND_LEVEL,
                    -> durationMinutesPerSet
                    else -> null
                },
                loadSpec = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD,
                    ExerciseWorkMode.TIME_SECONDS,
                    -> loadTrimmed
                    else -> null
                },
                loadKg = when (workMode) {
                    ExerciseWorkMode.REPS_LOAD,
                    ExerciseWorkMode.TIME_SECONDS,
                    -> parseSingleLoadKg(loadTrimmed)
                    else -> null
                },
                machineLevel = null,
                rowResistance = when (workMode) {
                    ExerciseWorkMode.DURATION_AND_LEVEL ->
                        rowResistance?.trim()?.takeIf { it.isNotEmpty() }
                    else -> null
                },
                workMode = workMode.storageKey,
                equipment = equipment.trim(),
                muscleGroupsCsv = muscles.toCsv(),
                restBetweenSetsSeconds = restSeconds.coerceAtLeast(0),
                createdAtMillis = bpExisting?.createdAtMillis ?: System.currentTimeMillis(),
            )
            if (exerciseId != null) {
                repo.updateExerciseBlueprint(
                    blueprint.copy(id = bpExisting!!.id, createdAtMillis = bpExisting.createdAtMillis),
                )
            } else {
                val newBpId = repo.insertExerciseBlueprint(blueprint.copy(id = 0L))
                repo.addTemplateExercise(
                    TemplateExerciseEntity(
                        templateId = templateId,
                        orderIndex = order,
                        exerciseId = newBpId,
                    ),
                )
            }
            onDone()
        }
    }

    companion object {
        fun factory(app: Application, templateId: Long, exerciseId: Long?) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TemplateExerciseEditorViewModel(app.requireGymRepository(), templateId, exerciseId) as T
            }
    }
}
