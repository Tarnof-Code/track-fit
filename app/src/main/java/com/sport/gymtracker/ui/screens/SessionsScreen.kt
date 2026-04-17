package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.ui.components.NewSessionDialog
import com.sport.gymtracker.ui.theme.sessionCompletedCardColors
import com.sport.gymtracker.ui.theme.sessionInProgressAccent
import com.sport.gymtracker.ui.theme.sessionInProgressCardBackground
import com.sport.gymtracker.ui.viewmodel.SessionsViewModel
import java.util.Calendar

/** Ex. « Dim12/04/2026 » (sans espace entre l’abréviation et le jour) */
private fun formatSessionDayDateLineFr(millis: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    val dow = when (c.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lun"
        Calendar.TUESDAY -> "Mar"
        Calendar.WEDNESDAY -> "Mer"
        Calendar.THURSDAY -> "Jeu"
        Calendar.FRIDAY -> "Ven"
        Calendar.SATURDAY -> "Sam"
        Calendar.SUNDAY -> "Dim"
        else -> ""
    }
    val d = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val m = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val y = c.get(Calendar.YEAR)
    return "$dow$d/$m/$y"
}

/** Ex. « 10h20 » */
private fun formatTimeHhMmFr(millis: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    val h = c.get(Calendar.HOUR_OF_DAY)
    val min = c.get(Calendar.MINUTE).toString().padStart(2, '0')
    return "${h}h$min"
}

/** Ex. « 1h25 », « 2h », « 40min », « 45s » */
private fun formatDurationPendantCompactFr(elapsedMs: Long): String {
    if (elapsedMs <= 0L) return "—"
    val totalMinutes = (elapsedMs / 60_000).toInt()
    if (totalMinutes < 1) {
        val sec = (elapsedMs / 1000).toInt().coerceAtLeast(1)
        return "${sec}s"
    }
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h${m.toString().padStart(2, '0')}"
        h > 0 -> "${h}h"
        else -> "${m}min"
    }
}

@Composable
fun SessionsScreen(
    onSessionClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val vm: SessionsViewModel = viewModel(factory = SessionsViewModel.factory(context.applicationContext as android.app.Application))
    val sessions by vm.sessions.collectAsState()
    val templateRows by vm.templateRows.collectAsState()
    var showNew by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Séances",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    "Un modèle contient tout un programme. Vous pouvez modifier le contenu des exercices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(sessions, key = { it.id }) { s ->
                val completed = s.endTimeMillis != null
                val completedPair = sessionCompletedCardColors()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClick(s.id) },
                    colors =
                        if (completed) {
                            CardDefaults.cardColors(
                                containerColor = completedPair.first,
                                contentColor = completedPair.second,
                            )
                        } else {
                            CardDefaults.cardColors(
                                containerColor = sessionInProgressCardBackground(),
                            )
                        },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(s.title, style = MaterialTheme.typography.titleMedium)
                        if (completed) {
                            val end = s.endTimeMillis!!
                            Text(
                                "${formatSessionDayDateLineFr(s.startTimeMillis)} pendant ${formatDurationPendantCompactFr(end - s.startTimeMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            Text(
                                "${formatSessionDayDateLineFr(s.startTimeMillis)} commencée à ${formatTimeHhMmFr(s.startTimeMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (completed) {
                            Text(
                                "Terminée",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            )
                        } else {
                            Text(
                                "En cours",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = sessionInProgressAccent(),
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp),
        )
        ExtendedFloatingActionButton(
            onClick = {
                if (sessions.any { it.endTimeMillis == null }) {
                    snackScope.launch {
                        snackbarHostState.showSnackbar(
                            "Une séance est déjà en cours. Terminez-la avant d’en démarrer une nouvelle.",
                        )
                    }
                } else {
                    showNew = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Séance") },
        )
    }

    if (showNew) {
        NewSessionDialog(
            templateRows = templateRows,
            onDismiss = { showNew = false },
            onLoadTemplatePreview = { templateId, onLoaded ->
                vm.loadTemplatePreviewForNewSession(templateId, onLoaded)
            },
            onCreate = { templateId ->
                vm.startSession(
                    templateId = templateId,
                    onCreated = { id ->
                        showNew = false
                        onSessionClick(id)
                    },
                )
            },
        )
    }

}
