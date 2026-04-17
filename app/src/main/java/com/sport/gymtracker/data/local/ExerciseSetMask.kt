package com.sport.gymtracker.data.local

/** Masque bit à bit des séries validées (bit 0 = série 1), jusqu’à 64 séries. */
fun fullExerciseSetsMask(setsCount: Int): Long {
    val n = setsCount.coerceAtLeast(1).coerceAtMost(64)
    return when (n) {
        64 -> -1L
        else -> (1L shl n) - 1L
    }
}

/** Masque des [completedCount] premières séries (préfixe), pour [totalSets] séries prévues au plus. */
fun completedPrefixMask(completedCount: Int, totalSets: Int): Long {
    val total = totalSets.coerceAtLeast(1).coerceAtMost(64)
    val c = completedCount.coerceIn(0, total)
    if (c == 0) return 0L
    if (c == total) return fullExerciseSetsMask(total)
    return (1L shl c) - 1L
}

/** Nombre de séries validées consécutivement depuis la série 1 (préfixe du masque). */
fun completedSetsPrefixCount(mask: Long, totalSets: Int): Int {
    val total = totalSets.coerceAtLeast(1).coerceAtMost(64)
    var k = 0
    while (k < total && (mask and (1L shl k)) != 0L) k++
    return k
}

private fun maskForCompletedCount(completed: Int, totalSets: Int): Long =
    completedPrefixMask(completed, totalSets)

/**
 * Si le clic est autorisé (ordre strict : valider 1 puis 2… ; décocher seulement la dernière validée),
 * retourne le nouveau masque, sinon null.
 */
fun nextMaskSequentialSetToggle(setIndex: Int, mask: Long, totalSets: Int): Long? {
    val total = totalSets.coerceAtLeast(1).coerceAtMost(64)
    val k = completedSetsPrefixCount(mask, total)
    val newCompleted = when {
        setIndex == k && k < total -> k + 1
        k > 0 && setIndex == k - 1 -> k - 1
        else -> return null
    }
    return maskForCompletedCount(newCompleted, total)
}

fun canToggleExerciseSetSequentially(setIndex: Int, mask: Long, totalSets: Int): Boolean =
    nextMaskSequentialSetToggle(setIndex, mask, totalSets) != null
