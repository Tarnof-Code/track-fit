package com.sport.gymtracker.data.local

import kotlin.math.max
import kotlin.math.min

/**
 * Nombre de « tours » validés dans une combinaison : chaque tour coche au plus une série par exercice ;
 * après [min(setsA, setsB)] tours communs, seul l’exercice avec le plus de séries continue.
 */
fun combinedSessionStepsCompleted(kA: Int, kB: Int, setsA: Int, setsB: Int): Int {
    val sa = setsA.coerceAtLeast(1).coerceAtMost(64)
    val sb = setsB.coerceAtLeast(1).coerceAtMost(64)
    val m = min(sa, sb)
    val ka = kA.coerceIn(0, sa)
    val kb = kB.coerceIn(0, sb)
    return min(ka, kb, m) + max(ka - m, kb - m, 0)
}

private fun min(a: Int, b: Int, c: Int): Int = minOf(a, b, c)

private fun max(a: Int, b: Int, c: Int): Int = maxOf(a, b, c)

/** Masques des deux entrées après [combinedCompleted] tours (voir [combinedSessionStepsCompleted]). */
fun sessionEntryMasksForCombinedSteps(
    combinedCompleted: Int,
    setsA: Int,
    setsB: Int,
): Pair<Long, Long> {
    val sa = setsA.coerceAtLeast(1).coerceAtMost(64)
    val sb = setsB.coerceAtLeast(1).coerceAtMost(64)
    val m = min(sa, sb)
    val maxSteps = max(sa, sb)
    val c = combinedCompleted.coerceIn(0, maxSteps)
    val (prefixA, prefixB) =
        if (c <= m) {
            c to c
        } else if (sa >= sb) {
            (c + m - sb) to sb
        } else {
            sa to (c + m - sa)
        }
    return completedPrefixMask(prefixA, sa) to completedPrefixMask(prefixB, sb)
}
