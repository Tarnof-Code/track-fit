package com.sport.gymtracker.data

/** Une séance terminée dans la semaine civile lundi–dimanche. */
data class WeekSessionInfo(
    val dayLabel: String,
    val templateOrTitle: String,
)

data class MuscleFrequencyItem(
    val muscleKey: String,
    val labelFr: String,
    val count: Int,
    /** Largeur relative de la barre (count / max des counts). */
    val fractionOfMax: Float,
)

data class StatisticsOverview(
    val weekRangeLabel: String,
    val currentWeekSessionCount: Int,
    val currentWeekSessions: List<WeekSessionInfo>,
    val avgSessionsPerWeek: Float,
    val avgSessionsPerMonth: Float,
    val avgGymMinutesPerWeek: Float,
    val avgGymMinutesPerMonth: Float,
    val muscleFrequencies: List<MuscleFrequencyItem>,
    val hasData: Boolean,
) {
    companion object {
        fun empty() = StatisticsOverview(
            weekRangeLabel = "",
            currentWeekSessionCount = 0,
            currentWeekSessions = emptyList(),
            avgSessionsPerWeek = 0f,
            avgSessionsPerMonth = 0f,
            avgGymMinutesPerWeek = 0f,
            avgGymMinutesPerMonth = 0f,
            muscleFrequencies = emptyList(),
            hasData = false,
        )
    }
}
