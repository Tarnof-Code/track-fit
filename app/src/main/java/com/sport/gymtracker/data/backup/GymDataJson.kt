package com.sport.gymtracker.data.backup

import com.sport.gymtracker.data.local.ExerciseBlueprintEntity
import com.sport.gymtracker.data.local.ExerciseEntryEntity
import com.sport.gymtracker.data.local.TemplateExerciseEntity
import com.sport.gymtracker.data.local.WorkoutSessionEntity
import com.sport.gymtracker.data.local.WorkoutTemplateEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val GYM_DATA_JSON_FORMAT_VERSION = 1

private val jsonEncoder = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
data class GymDataFile(
    val formatVersion: Int = GYM_DATA_JSON_FORMAT_VERSION,
    val exportedAtMillis: Long,
    val blueprints: List<BlueprintJson>,
    val templates: List<TemplateJson>,
    val templateExercises: List<TemplateExerciseJson>,
    val sessions: List<SessionJson>,
    val exerciseEntries: List<ExerciseEntryJson>,
)

@Serializable
data class BlueprintJson(
    val id: Long,
    val name: String,
    val notes: String = "",
    val sets: Int,
    val repsPerSet: Int? = null,
    val durationSecondsPerSet: Int? = null,
    val durationMinutesPerSet: Int? = null,
    val loadSpec: String? = null,
    val loadKg: Float? = null,
    val machineLevel: Int? = null,
    val rowResistance: String? = null,
    val workMode: String,
    val equipment: String,
    val muscleGroupsCsv: String,
    val restBetweenSetsSeconds: Int,
    val createdAtMillis: Long,
)

@Serializable
data class TemplateJson(
    val id: Long,
    val name: String,
    val description: String? = null,
    val createdAtMillis: Long,
)

@Serializable
data class TemplateExerciseJson(
    val id: Long,
    val templateId: Long,
    val orderIndex: Int,
    val exerciseId: Long,
)

@Serializable
data class SessionJson(
    val id: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val title: String,
    val sourceTemplateId: Long? = null,
)

@Serializable
data class ExerciseEntryJson(
    val id: Long,
    val sessionId: Long,
    val orderIndex: Int,
    val exerciseId: Long,
    val difficulty: String,
    val level: String,
    val doneInSession: Boolean = false,
    val perfCapturedAtMillis: Long? = null,
    val perfWorkMode: String? = null,
    val perfSets: Int? = null,
    val perfRepsPerSet: Int? = null,
    val perfDurationSecondsPerSet: Int? = null,
    val perfDurationMinutesPerSet: Int? = null,
    val perfLoadKg: Float? = null,
    val perfLoadSpec: String? = null,
    val perfRowResistance: String? = null,
)

fun ExerciseBlueprintEntity.toBlueprintJson(): BlueprintJson =
    BlueprintJson(
        id = id,
        name = name,
        notes = notes,
        sets = sets,
        repsPerSet = repsPerSet,
        durationSecondsPerSet = durationSecondsPerSet,
        durationMinutesPerSet = durationMinutesPerSet,
        loadSpec = loadSpec,
        loadKg = loadKg,
        machineLevel = machineLevel,
        rowResistance = rowResistance,
        workMode = workMode,
        equipment = equipment,
        muscleGroupsCsv = muscleGroupsCsv,
        restBetweenSetsSeconds = restBetweenSetsSeconds,
        createdAtMillis = createdAtMillis,
    )

fun BlueprintJson.toEntity(): ExerciseBlueprintEntity =
    ExerciseBlueprintEntity(
        id = 0L,
        name = name,
        notes = notes,
        sets = sets,
        repsPerSet = repsPerSet,
        durationSecondsPerSet = durationSecondsPerSet,
        durationMinutesPerSet = durationMinutesPerSet,
        loadSpec = loadSpec,
        loadKg = loadKg,
        machineLevel = machineLevel,
        rowResistance = rowResistance,
        workMode = workMode,
        equipment = equipment,
        muscleGroupsCsv = muscleGroupsCsv,
        restBetweenSetsSeconds = restBetweenSetsSeconds,
        createdAtMillis = createdAtMillis,
    )

fun WorkoutTemplateEntity.toTemplateJson(): TemplateJson =
    TemplateJson(
        id = id,
        name = name,
        description = description,
        createdAtMillis = createdAtMillis,
    )

fun TemplateExerciseEntity.toTemplateExerciseJson(): TemplateExerciseJson =
    TemplateExerciseJson(
        id = id,
        templateId = templateId,
        orderIndex = orderIndex,
        exerciseId = exerciseId,
    )

fun WorkoutSessionEntity.toSessionJson(): SessionJson =
    SessionJson(
        id = id,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        title = title,
        sourceTemplateId = sourceTemplateId,
    )

fun ExerciseEntryEntity.toExerciseEntryJson(): ExerciseEntryJson =
    ExerciseEntryJson(
        id = id,
        sessionId = sessionId,
        orderIndex = orderIndex,
        exerciseId = exerciseId,
        difficulty = difficulty,
        level = level,
        doneInSession = doneInSession,
        perfCapturedAtMillis = perfCapturedAtMillis,
        perfWorkMode = perfWorkMode,
        perfSets = perfSets,
        perfRepsPerSet = perfRepsPerSet,
        perfDurationSecondsPerSet = perfDurationSecondsPerSet,
        perfDurationMinutesPerSet = perfDurationMinutesPerSet,
        perfLoadKg = perfLoadKg,
        perfLoadSpec = perfLoadSpec,
        perfRowResistance = perfRowResistance,
    )

fun ExerciseEntryJson.toEntity(sessionId: Long, exerciseId: Long): ExerciseEntryEntity =
    ExerciseEntryEntity(
        id = 0L,
        sessionId = sessionId,
        orderIndex = orderIndex,
        exerciseId = exerciseId,
        difficulty = difficulty,
        level = level,
        doneInSession = doneInSession,
        perfCapturedAtMillis = perfCapturedAtMillis,
        perfWorkMode = perfWorkMode,
        perfSets = perfSets,
        perfRepsPerSet = perfRepsPerSet,
        perfDurationSecondsPerSet = perfDurationSecondsPerSet,
        perfDurationMinutesPerSet = perfDurationMinutesPerSet,
        perfLoadKg = perfLoadKg,
        perfLoadSpec = perfLoadSpec,
        perfRowResistance = perfRowResistance,
    )

fun encodeGymDataJson(data: GymDataFile): String = jsonEncoder.encodeToString(data)

fun decodeGymDataJson(json: String): GymDataFile = jsonEncoder.decodeFromString(json)

fun validateGymDataForImport(data: GymDataFile) {
    validateGymDataForImport(data, ImportContentScope.ALL)
}

fun validateGymDataForImport(data: GymDataFile, scope: ImportContentScope) {
    when (scope) {
        ImportContentScope.ALL -> validateFullGymDataFile(data)
        ImportContentScope.EXERCISES_ONLY -> { }
        ImportContentScope.TEMPLATES_AND_EXERCISES -> validateTemplatesSlice(data)
    }
}

private fun validateFullGymDataFile(data: GymDataFile) {
    val blueprintIds = data.blueprints.map { it.id }.toSet()
    val templateIds = data.templates.map { it.id }.toSet()
    val sessionIds = data.sessions.map { it.id }.toSet()
    for (te in data.templateExercises) {
        require(te.templateId in templateIds) {
            "Ligne de modèle référence un modèle absent (id=${te.templateId})."
        }
        require(te.exerciseId in blueprintIds) {
            "Ligne de modèle référence un exercice absent (exerciseId=${te.exerciseId})."
        }
    }
    for (s in data.sessions) {
        val st = s.sourceTemplateId
        if (st != null) {
            require(st in templateIds) {
                "Séance « ${s.title} » référence un modèle absent (sourceTemplateId=$st)."
            }
        }
    }
    for (e in data.exerciseEntries) {
        require(e.sessionId in sessionIds) {
            "Entrée d’exercice référence une séance absente (sessionId=${e.sessionId})."
        }
        require(e.exerciseId in blueprintIds) {
            "Entrée d’exercice référence une fiche absente (exerciseId=${e.exerciseId})."
        }
    }
}

private fun validateTemplatesSlice(data: GymDataFile) {
    val blueprintIds = data.blueprints.map { it.id }.toSet()
    val templateIds = data.templates.map { it.id }.toSet()
    for (te in data.templateExercises) {
        require(te.templateId in templateIds) {
            "Ligne de modèle référence un modèle absent (id=${te.templateId})."
        }
        require(te.exerciseId in blueprintIds) {
            "Ligne de modèle référence un exercice absent (exerciseId=${te.exerciseId})."
        }
    }
}
