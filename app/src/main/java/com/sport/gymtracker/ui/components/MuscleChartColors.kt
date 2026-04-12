package com.sport.gymtracker.ui.components

import androidx.compose.ui.graphics.Color
import com.sport.gymtracker.domain.MuscleGroup

private val musclePalette = listOf(
    Color(0xFFE57373),
    Color(0xFF81C784),
    Color(0xFF64B5F6),
    Color(0xFFFFB74D),
    Color(0xFFBA68C8),
    Color(0xFF4DD0E1),
    Color(0xFFFFF176),
    Color(0xFFA1887F),
    Color(0xFF90A4AE),
    Color(0xFFF06292),
    Color(0xFF7986CB),
    Color(0xFF4DB6AC),
    Color(0xFFFF8A65),
)

fun colorForMuscleStorageKey(key: String): Color {
    val idx = MuscleGroup.entries.indexOfFirst { it.name == key }
    return if (idx >= 0) musclePalette[idx % musclePalette.size] else Color(0xFF9E9E9E)
}
