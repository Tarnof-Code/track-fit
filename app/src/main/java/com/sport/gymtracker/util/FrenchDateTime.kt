package com.sport.gymtracker.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val french: Locale = Locale.FRENCH

/** Formatteurs réutilisables (thread-safe), fuseau système pour les timestamps stockés en local. */
object FrenchDateTime {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    private val weekdayFullDate: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", french)
    private val sessionDetail: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy 'à' HH:mm", french)
    private val backupDay: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", french)
    private val chartDay: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM", french)
    private val statsRangeDay: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", french)
    private val statsWeekdayShort: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE d MMM", french)

    fun formatWeekdayFullDate(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(weekdayFullDate)

    fun formatSessionDetail(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(sessionDetail)

    fun formatBackupDay(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(backupDay)

    fun formatChartDay(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(chartDay)

    fun formatStatsRangeDay(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(statsRangeDay)

    fun formatStatsWeekdayShort(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(statsWeekdayShort)
}
