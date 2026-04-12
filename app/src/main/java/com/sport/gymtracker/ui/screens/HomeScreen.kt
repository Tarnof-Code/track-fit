package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.R
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.ui.components.NewSessionDialog
import com.sport.gymtracker.ui.viewmodel.HomeViewModel
import com.sport.gymtracker.ui.viewmodel.SessionsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSession: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    val sessionsVm: SessionsViewModel = viewModel(factory = SessionsViewModel.factory(app))
    val state by vm.home.collectAsState()
    val templateRows by sessionsVm.templateRows.collectAsState()
    var showNewSession by remember { mutableStateOf(false) }
    val lastSessionDateFmt = remember { SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)

        state?.let { h ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cette semaine : ${h.sessionsThisWeek} séance(s)")
                    if (h.topMusclesThisWeek.isNotEmpty()) {
                        Text(
                            "Muscles travaillés :\n" +
                                h.topMusclesThisWeek.joinToString { p ->
                                    MuscleGroup.entries.find { m -> m.name == p.first }?.labelFr ?: p.first
                                },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dernière séance", style = MaterialTheme.typography.titleMedium)
                    val lastMs = h.lastCompletedSessionStartMillis
                    if (lastMs != null) {
                        Text(
                            lastSessionDateFmt.format(Date(lastMs)),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        val rest = h.restDaysSinceLastSession
                        Text(
                            when (rest) {
                                0 -> "Aucun jour de repos (séance aujourd’hui)."
                                1 -> "1 jour de repos depuis cette séance."
                                else -> "$rest jours de repos depuis cette séance."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            "Terminez une séance pour afficher la date de la dernière ici.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = { showNewSession = true },
                modifier = Modifier.size(176.dp),
                shape = CircleShape,
                contentPadding = ButtonDefaults.ContentPadding,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            ) {
                Text(
                    "Nouvelle\nséance",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    if (showNewSession) {
        NewSessionDialog(
            templateRows = templateRows,
            onDismiss = { showNewSession = false },
            onCreate = { templateId ->
                sessionsVm.startSession(templateId) { id ->
                    showNewSession = false
                    onOpenSession(id)
                }
            },
        )
    }
}
