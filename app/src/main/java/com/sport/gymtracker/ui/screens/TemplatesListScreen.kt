package com.sport.gymtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.ui.components.CompactSearchField
import com.sport.gymtracker.ui.viewmodel.TemplatesListViewModel

@Composable
fun TemplatesListScreen(
    onTemplateClick: (Long) -> Unit,
    onOpenExerciseLibrary: () -> Unit,
) {
    val context = LocalContext.current
    val vm: TemplatesListViewModel = viewModel(
        factory = TemplatesListViewModel.factory(context.applicationContext as android.app.Application),
    )
    val templateRows by vm.templateRows.collectAsState()
    var showNew by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val filteredRows =
        remember(templateRows, searchQuery) {
            val q = searchQuery.trim().lowercase(Locale.FRENCH)
            if (q.isEmpty()) {
                templateRows
            } else {
                templateRows.filter { t ->
                    t.name.lowercase(Locale.FRENCH).contains(q) ||
                        t.description?.lowercase(Locale.FRENCH)?.contains(q) == true
                }
            }
        }

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
                    "Modèles de séance",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Button(
                    onClick = onOpenExerciseLibrary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Bibliothèque d’exercices réutilisables",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                CompactSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Rechercher un modèle",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
            }
            if (filteredRows.isEmpty() && templateRows.isNotEmpty()) {
                item {
                    Text(
                        "Aucun modèle ne correspond à ta recherche.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            items(filteredRows, key = { it.id }) { t ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTemplateClick(t.id) },
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(t.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${t.exerciseCount} exercice(s) dans ce programme",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        t.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
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
            text = { Text("Modèle") },
        )
    }

    if (showNew) {
        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("Nouveau modèle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Ensuite tu pourras ajouter tous les exercices du programme avec le bouton +.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom du modèle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.createTemplate(name, null) { id ->
                            showNew = false
                            name = ""
                            onTemplateClick(id)
                        }
                    },
                ) { Text("Créer") }
            },
            dismissButton = {
                TextButton(onClick = { showNew = false }) { Text("Annuler") }
            },
        )
    }
}
