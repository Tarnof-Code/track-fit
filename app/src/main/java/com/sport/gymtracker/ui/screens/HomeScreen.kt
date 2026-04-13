package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.R
import com.sport.gymtracker.domain.MuscleGroup
import com.sport.gymtracker.ui.components.NewSessionDialog
import com.sport.gymtracker.ui.viewmodel.HomeViewModel
import com.sport.gymtracker.ui.viewmodel.SessionsViewModel
import com.sport.gymtracker.util.FrenchDateTime

@Composable
fun HomeScreen(
    onOpenSession: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    val sessionsVm: SessionsViewModel = viewModel(factory = SessionsViewModel.factory(app))
    val state by vm.home.collectAsState()
    val sessions by sessionsVm.sessions.collectAsState()
    val templateRows by sessionsVm.templateRows.collectAsState()
    var showNewSession by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.app_name))
                    append(" by Tarnof")   
                },
                style = MaterialTheme.typography.headlineMedium,
            )
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
                                FrenchDateTime.formatWeekdayFullDate(lastMs),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    onClick = {
                        if (sessions.any { it.endTimeMillis == null }) {
                            snackScope.launch {
                                snackbarHostState.showSnackbar(
                                    "Une séance est déjà en cours. Terminez-la avant d’en démarrer une nouvelle.",
                                )
                            }
                        } else {
                            showNewSession = true
                        }
                    },
                    modifier = Modifier.size(168.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 4.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Nouvelle\nséance",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )
    }

    if (showNewSession) {
        NewSessionDialog(
            templateRows = templateRows,
            onDismiss = { showNewSession = false },
            onLoadTemplatePreview = { templateId, onLoaded ->
                sessionsVm.loadTemplatePreviewForNewSession(templateId, onLoaded)
            },
            onCreate = { templateId ->
                sessionsVm.startSession(
                    templateId = templateId,
                    onCreated = { id ->
                        showNewSession = false
                        onOpenSession(id)
                    },
                )
            },
        )
    }
}
