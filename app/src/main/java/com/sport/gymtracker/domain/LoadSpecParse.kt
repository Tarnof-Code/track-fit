package com.sport.gymtracker.domain

/**
 * Si la saisie correspond à **une seule** masse (pas de liste ni d’intervalle),
 * retourne la valeur pour [com.sport.gymtracker.data.local.ExerciseBlueprintEntity.loadKg].
 */
fun parseSingleLoadKg(loadSpec: String?): Float? {
    val s0 = loadSpec?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val range = Regex("""^\d+(?:\.\d+)?\s*[-–—]\s*\d+(?:\.\d+)?$""")
    if (range.matches(s0)) return null
    if (Regex("""\d\s*,\s*\d""").containsMatchIn(s0)) return null
    val forParse = s0.replace(',', '.')
    return forParse.toFloatOrNull()
}
