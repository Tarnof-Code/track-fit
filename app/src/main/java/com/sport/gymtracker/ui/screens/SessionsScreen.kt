package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.ui.components.NewSessionDialog
import com.sport.gymtracker.ui.viewmodel.SessionsViewModel
import java.util.Calendar

private val SessionCardCompletedGreen = Color(0xFFE8F5E9)
private val SessionInProgressRed = Color(0xFFFF1744)

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
fun SessionsScreen(onSessionClick: (Long) -> Unit) {
    val context = LocalContext.current
    val vm: SessionsViewModel = viewModel(factory = SessionsViewModel.factory(context.applicationContext as android.app.Application))
    val sessions by vm.sessions.collectAsState()
    val templateRows by vm.templateRows.collectAsState()
    var showNew by remember { mutableStateOf(false) }

    var rowMenuSessionId by remember { mutableStateOf<Long?>(null) }
    var deleteSessionId by remember { mutableStateOf<Long?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Séances", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Un modèle contient tout un programme. Vous pouvez modifier le contenu des exercices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sessions, key = { it.id }) { s ->
                    val completed = s.endTimeMillis != null
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (completed) {
                                SessionCardCompletedGreen
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(
                                        if (!completed) {
                                            Modifier.clickable { onSessionClick(s.id) }
                                        } else {
                                            Modifier
                                        },
                                    )
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
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    Text(
                                        "En cours",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SessionInProgressRed,
                                    )
                                }
                            }
                            if (completed) {
                                Box {
                                    IconButton(onClick = { rowMenuSessionId = s.id }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                                    }
                                    DropdownMenu(
                                        expanded = rowMenuSessionId == s.id,
                                        onDismissRequest = { rowMenuSessionId = null },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Modifier") },
                                            onClick = {
                                                rowMenuSessionId = null
                                                onSessionClick(s.id)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Supprimer") },
                                            onClick = {
                                                deleteSessionId = s.id
                                                rowMenuSessionId = null
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showNew = true },
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
            onCreate = { templateId ->
                vm.startSession(templateId) { id ->
                    showNew = false
                    onSessionClick(id)
                }
            },
        )
    }

    val dsId = deleteSessionId
    if (dsId != null) {
        AlertDialog(
            onDismissRequest = { deleteSessionId = null },
            title = { Text("Supprimer cette séance ?") },
            text = { Text("Tous les exercices enregistrés pour cette séance seront supprimés.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteSession(dsId)
                        deleteSessionId = null
                    },
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { deleteSessionId = null }) { Text("Annuler") }
            },
        )
    }
}
