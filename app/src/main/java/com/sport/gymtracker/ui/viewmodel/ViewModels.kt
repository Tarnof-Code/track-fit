package com.sport.gymtracker.ui.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sport.gymtracker.data.backup.DataImportMode
import com.sport.gymtracker.data.backup.DataImportResult
import com.sport.gymtracker.data.backup.ImportContentScope
import com.sport.gymtracker.data.ExerciseProgressListItem
import com.sport.gymtracker.data.GymRepository
import com.sport.gymtracker.data.TemplatePreviewForSession
import com.sport.gymtracker.data.StartSessionResult
import com.sport.gymtracker.data.HomeState
import com.sport.gymtracker.data.StatisticsOverview
import com.sport.gymtracker.data.local.ExerciseEntryEntity
import com.sport.gymtracker.data.local.completedSetsPrefixCount
import com.sport.gymtracker.data.local.nextMaskSequentialSetToggle
import com.sport.gymtracker.data.local.ExercisePerformanceHistoryRow
import com.sport.gymtracker.data.local.TemplateExerciseEntity
import com.sport.gymtracker.data.local.exerciseBlueprintFromEditorInput
import com.sport.gymtracker.domain.Difficulty
import com.sport.gymtracker.domain.ExerciseEditorSaveParams
import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.domain.SkillLevel
import com.sport.gymtracker.domain.recommendedRestSeconds
import com.sport.gymtracker.requireGymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val WhileSubscribed5s = SharingStarted.WhileSubscribed(5_000)

class HomeViewModel(private val repo: GymRepository) : ViewModel() {
    val home: StateFlow<HomeState?> = repo.observeHomeState()
        .stateIn(viewModelScope, WhileSubscribed5s, null)

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
        .stateIn(viewModelScope, WhileSubscribed5s, StatisticsOverview.empty())

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
    val blueprintNotes: String,
    val workMode: ExerciseWorkMode,
    val history: List<ExercisePerformanceHistoryRow>,
)

class ExerciseProgressListViewModel(private val repo: GymRepository) : ViewModel() {
    val items: StateFlow<List<ExerciseProgressListItem>> =
        repo.observeExerciseProgressList()
            .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

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
                blueprintNotes = bp.notes,
                workMode = ExerciseWorkMode.fromStorage(bp.workMode),
                history = hist,
            )
        }.stateIn(viewModelScope, WhileSubscribed5s, null)

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
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

    val templateRows = repo.observeTemplateListRows()
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

    fun startSession(
        templateId: Long?,
        onCreated: (Long) -> Unit,
        onBlocked: () -> Unit = {},
    ) {
        viewModelScope.launch {
            when (val r = repo.startSession(templateId)) {
                is StartSessionResult.Created -> onCreated(r.sessionId)
                StartSessionResult.ActiveSessionExists -> onBlocked()
            }
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repo.deleteSession(id) }
    }

    fun loadTemplatePreviewForNewSession(
        templateId: Long,
        onLoaded: (TemplatePreviewForSession?) -> Unit,
    ) {
        viewModelScope.launch {
            onLoaded(repo.loadTemplatePreviewForNewSession(templateId))
        }
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
        .stateIn(viewModelScope, WhileSubscribed5s, null)

    val exercises = repo.observeSessionExerciseListItems(sessionId)
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

    val exerciseBlueprints = repo.observeExerciseBlueprints()
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

    /**
     * Fin du repos (SystemClock.elapsedRealtime), ou null si aucun overlay.
     * Conservé dans le ViewModel pour survivre à la rotation de l’écran.
     */
    private val _restOverlayEndElapsedMs = MutableStateFlow<Long?>(null)
    val restOverlayEndElapsedMs: StateFlow<Long?> = _restOverlayEndElapsedMs.asStateFlow()

    fun dismissRestOverlay() {
        _restOverlayEndElapsedMs.value = null
    }

    fun addExerciseFromBlueprint(blueprintId: Long) {
        viewModelScope.launch {
            repo.addExerciseFromBlueprintToSession(sessionId, blueprintId)
        }
    }

    /** Ajoute plusieurs exercices dans l’ordre donné (sélection depuis la bibliothèque). */
    fun addExercisesFromBlueprints(blueprintIds: List<Long>) {
        if (blueprintIds.isEmpty()) return
        viewModelScope.launch {
            repo.addBlueprintsToSessionInOrder(sessionId, blueprintIds)
        }
    }

    fun endSession() {
        viewModelScope.launch { repo.endSession(sessionId) }
    }

    fun saveSessionAsTemplate(name: String, description: String?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.saveSessionAsTemplate(sessionId, name, description)
            onCreated(id)
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            repo.deleteExercise(id)
        }
    }

    fun combineSessionExercises(entryIdA: Long, entryIdB: Long) {
        viewModelScope.launch {
            repo.combineSessionExercises(entryIdA, entryIdB)
        }
    }

    fun splitSessionCombo(entryId: Long) {
        viewModelScope.launch {
            repo.splitSessionExerciseCombo(entryId)
        }
    }

    fun setComboExerciseDone(entryIdA: Long, entryIdB: Long, done: Boolean) {
        viewModelScope.launch {
            repo.updateExerciseDoneInSession(entryIdA, done)
            repo.updateExerciseDoneInSession(entryIdB, done)
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
            if (done) {
                val bp = repo.getExerciseBlueprint(ex.exerciseId) ?: return@launch
                if (completedSetsPrefixCount(ex.completedSetsMask, bp.sets) == 0) return@launch
            }
            repo.updateExerciseDoneInSession(exerciseId, done)
        }
    }

    /** Bascule une série ; lance le repos plein écran si besoin (sauf après la dernière série). */
    fun onExerciseSetClicked(entryId: Long, setIndex: Int) {
        viewModelScope.launch {
            val entry = repo.getExercise(entryId) ?: return@launch
            if (entry.sessionId != sessionId) return@launch
            if (entry.comboGroupId != null) return@launch
            if (session.value?.endTimeMillis != null) return@launch
            if (entry.doneInSession) return@launch
            val bp = repo.getExerciseBlueprint(entry.exerciseId) ?: return@launch
            val sets = bp.sets.coerceAtLeast(1).coerceAtMost(64)
            if (setIndex !in 0 until sets) return@launch
            val old = entry.completedSetsMask
            val kBefore = completedSetsPrefixCount(old, sets)
            val newMask = nextMaskSequentialSetToggle(setIndex, old, sets) ?: return@launch
            val turningOn = setIndex == kBefore
            repo.updateExerciseCompletedSetsMask(entryId, newMask)
            if (!turningOn) return@launch
            val lastSetIndex = sets - 1
            if (setIndex < lastSetIndex && bp.restBetweenSetsSeconds > 0) {
                _restOverlayEndElapsedMs.value =
                    SystemClock.elapsedRealtime() + bp.restBetweenSetsSeconds * 1000L
            }
        }
    }

    fun onComboSetClicked(entryIdA: Long, entryIdB: Long, setIndex: Int) {
        viewModelScope.launch {
            if (session.value?.endTimeMillis != null) return@launch
            val restSec = repo.toggleComboSessionSet(entryIdA, entryIdB, setIndex) ?: return@launch
            _restOverlayEndElapsedMs.value =
                SystemClock.elapsedRealtime() + restSec * 1000L
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
        notes: String,
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
            if (existing?.doneInSession == true) return@launch
            val order = if (exerciseId != null) {
                existing?.orderIndex ?: repo.nextOrderIndex(sessionId)
            } else {
                repo.nextOrderIndex(sessionId)
            }
            val bpExisting = existing?.let { repo.getExerciseBlueprint(it.exerciseId) }
            val blueprint = exerciseBlueprintFromEditorInput(
                workMode = workMode,
                name = name,
                notes = notes,
                sets = sets,
                repsPerSet = repsPerSet,
                durationSecondsPerSet = durationSecondsPerSet,
                durationMinutesPerSet = durationMinutesPerSet,
                loadSpec = loadSpec,
                rowResistance = rowResistance,
                equipment = equipment,
                muscles = muscles,
                restSeconds = restSeconds,
                blueprintId = bpExisting?.id ?: 0L,
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

    fun save(params: ExerciseEditorSaveParams, onDone: () -> Unit) {
        save(
            workMode = params.workMode,
            name = params.name,
            notes = params.notes,
            sets = params.sets,
            repsPerSet = params.repsPerSet,
            durationSecondsPerSet = params.durationSecondsPerSet,
            durationMinutesPerSet = params.durationMinutesPerSet,
            loadSpec = params.loadSpec,
            rowResistance = params.rowResistance,
            equipment = params.equipment,
            muscles = params.muscles,
            restSeconds = params.restSeconds,
            onDone = onDone,
        )
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
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

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
        .stateIn(viewModelScope, WhileSubscribed5s, null)

    val exercises = repo.observeTemplateExerciseLines(templateId)
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

    val exerciseBlueprints = repo.observeExerciseBlueprints()
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

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

    /** Ajoute plusieurs exercices dans l’ordre donné (sélection depuis la bibliothèque). */
    fun addExercisesFromBlueprints(blueprintIds: List<Long>) {
        if (blueprintIds.isEmpty()) return
        viewModelScope.launch {
            repo.addBlueprintsToTemplateInOrder(templateId, blueprintIds)
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
        .stateIn(viewModelScope, WhileSubscribed5s, emptyList())

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
        notes: String,
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
            val now = System.currentTimeMillis()
            if (blueprintId == 0L) {
                repo.insertExerciseBlueprint(
                    exerciseBlueprintFromEditorInput(
                        workMode = workMode,
                        name = name,
                        notes = notes,
                        sets = sets,
                        repsPerSet = repsPerSet,
                        durationSecondsPerSet = durationSecondsPerSet,
                        durationMinutesPerSet = durationMinutesPerSet,
                        loadSpec = loadSpec,
                        rowResistance = rowResistance,
                        equipment = equipment,
                        muscles = muscles,
                        restSeconds = restSeconds,
                        blueprintId = 0L,
                        createdAtMillis = now,
                    ),
                )
            } else {
                val existing = repo.getExerciseBlueprint(blueprintId) ?: return@launch
                repo.updateExerciseBlueprint(
                    exerciseBlueprintFromEditorInput(
                        workMode = workMode,
                        name = name,
                        notes = notes,
                        sets = sets,
                        repsPerSet = repsPerSet,
                        durationSecondsPerSet = durationSecondsPerSet,
                        durationMinutesPerSet = durationMinutesPerSet,
                        loadSpec = loadSpec,
                        rowResistance = rowResistance,
                        equipment = equipment,
                        muscles = muscles,
                        restSeconds = restSeconds,
                        blueprintId = existing.id,
                        createdAtMillis = existing.createdAtMillis,
                    ),
                )
            }
            onDone()
        }
    }

    fun save(params: ExerciseEditorSaveParams, onDone: () -> Unit) {
        save(
            workMode = params.workMode,
            name = params.name,
            notes = params.notes,
            sets = params.sets,
            repsPerSet = params.repsPerSet,
            durationSecondsPerSet = params.durationSecondsPerSet,
            durationMinutesPerSet = params.durationMinutesPerSet,
            loadSpec = params.loadSpec,
            rowResistance = params.rowResistance,
            equipment = params.equipment,
            muscles = params.muscles,
            restSeconds = params.restSeconds,
            onDone = onDone,
        )
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
        notes: String,
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
            val blueprint = exerciseBlueprintFromEditorInput(
                workMode = workMode,
                name = name,
                notes = notes,
                sets = sets,
                repsPerSet = repsPerSet,
                durationSecondsPerSet = durationSecondsPerSet,
                durationMinutesPerSet = durationMinutesPerSet,
                loadSpec = loadSpec,
                rowResistance = rowResistance,
                equipment = equipment,
                muscles = muscles,
                restSeconds = restSeconds,
                blueprintId = bpExisting?.id ?: 0L,
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

    fun save(params: ExerciseEditorSaveParams, onDone: () -> Unit) {
        save(
            workMode = params.workMode,
            name = params.name,
            notes = params.notes,
            sets = params.sets,
            repsPerSet = params.repsPerSet,
            durationSecondsPerSet = params.durationSecondsPerSet,
            durationMinutesPerSet = params.durationMinutesPerSet,
            loadSpec = params.loadSpec,
            rowResistance = params.rowResistance,
            equipment = params.equipment,
            muscles = params.muscles,
            restSeconds = params.restSeconds,
            onDone = onDone,
        )
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
