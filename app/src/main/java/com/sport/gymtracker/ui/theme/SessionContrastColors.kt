package com.sport.gymtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Détecte le mode sombre **de l’app** (thème Material), pas seulement le réglage système.
 */
@Composable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

/** Carte « séance terminée » / « exercice validé » : fond + texte lisibles en clair et en sombre. */
@Composable
fun sessionCompletedCardColors(): Pair<Color, Color> {
    return if (isAppInDarkTheme()) {
        Color(0xFF1E3A2E) to Color(0xFFF0FFF4)
    } else {
        Color(0xFFE8F5E9) to Color(0xFF1B5E20)
    }
}

/** Fond carte « séance en cours » (liste séances) / exercice non validé (détail). */
@Composable
fun sessionInProgressCardBackground(): Color {
    return if (isAppInDarkTheme()) {
        Color(0xFF4A2E32)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
}

/** Libellé « En cours » : rouge lisible sur fond clair et sur fond sombre. */
@Composable
fun sessionInProgressAccent(): Color {
    return if (isAppInDarkTheme()) {
        Color(0xFFFFD2CC)
    } else {
        Color(0xFFB71C1C)
    }
}

/** Icône check sur exercice validé (détail séance). */
@Composable
fun exerciseDoneCheckIconTint(): Color {
    return if (isAppInDarkTheme()) {
        Color(0xFF69F0AE)
    } else {
        Color(0xFF2E7D32)
    }
}
