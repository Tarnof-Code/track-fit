package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.domain.exerciseTypeLabelFr
import com.sport.gymtracker.domain.prescriptionSummaryShort
import com.sport.gymtracker.ui.components.CompactSearchField
import com.sport.gymtracker.ui.viewmodel.ExerciseLibraryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    onEditBlueprint: (Long) -> Unit,
) {
    val context = LocalContext.current
    val vm: ExerciseLibraryViewModel = viewModel(
        factory = ExerciseLibraryViewModel.factory(context.applicationContext as android.app.Application),
    )
    val blueprints by vm.blueprints.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredBlueprints =
        remember(blueprints, searchQuery) {
            val q = searchQuery.trim().lowercase(Locale.FRENCH)
            if (q.isEmpty()) {
                blueprints
            } else {
                blueprints.filter { bp ->
                    bp.name.lowercase(Locale.FRENCH).contains(q) ||
                        bp.notes.lowercase(Locale.FRENCH).contains(q) ||
                        bp.prescriptionSummaryShort().lowercase(Locale.FRENCH).contains(q) ||
                        bp.equipment.lowercase(Locale.FRENCH).contains(q)
                }
            }
        }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    var noteDialog by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEditBlueprint(0L) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Exercice") },
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Bibliothèque d’exercices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Exercices réutilisables",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                CompactSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Rechercher un exercice",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
            if (filteredBlueprints.isEmpty() && blueprints.isNotEmpty()) {
                item {
                    Text(
                        "Aucun exercice ne correspond à ta recherche.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            items(filteredBlueprints, key = { it.id }) { bp ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                bp.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                            )
                            if (bp.notes.isNotBlank()) {
                                IconButton(
                                    onClick = { noteDialog = bp.notes },
                                ) {
                                    Icon(
                                        Icons.Filled.PushPin,
                                        contentDescription = "Afficher les notes",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        Text(
                            bp.exerciseTypeLabelFr(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            bp.prescriptionSummaryShort(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Modifier",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onEditBlueprint(bp.id) },
                            )
                            Text(
                                text = "Supprimer",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { deleteId = bp.id },
                            )
                        }
                    }
                }
            }
        }
    }

    noteDialog?.let { noteText ->
        AlertDialog(
            onDismissRequest = { noteDialog = null },
            title = { Text("Note") },
            text = {
                Text(
                    noteText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { noteDialog = null }) { Text("OK") }
            },
        )
    }

    val del = deleteId
    if (del != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Supprimer cet exercice ?") },
            text = {
                Text(
                    "La fiche sera supprimée définitivement. Tu ne peux supprimer que les exercices qui ne figurent " +
                        "plus dans aucun modèle ni aucune séance — retire-les d’abord si besoin.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteBlueprint(del) { ok ->
                            deleteId = null
                            if (!ok) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Suppression impossible : cet exercice est encore utilisé dans un modèle ou une séance.",
                                    )
                                }
                            }
                        }
                    },
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text("Annuler") }
            },
        )
    }
}
