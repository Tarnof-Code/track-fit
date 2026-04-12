package com.sport.gymtracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sport.gymtracker.data.backup.DataImportMode
import com.sport.gymtracker.data.backup.ImportContentScope
import com.sport.gymtracker.ui.viewmodel.HomeViewModel
import com.sport.gymtracker.util.FrenchDateTime

@Composable
fun BackupScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as android.app.Application
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var importHasExistingData by remember { mutableStateOf(true) }
    var importScope: ImportContentScope? by remember { mutableStateOf(null) }
    var scopeDraft by remember(pendingImportJson) { mutableStateOf(ImportContentScope.ALL) }
    var confirmReplaceImport by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successDialogText by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        vm.exportDataJson { result ->
            result.fold(
                onSuccess = { json ->
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("Impossible d’écrire le fichier.")
                    }.fold(
                        onSuccess = {
                            successDialogText = "Le fichier d’export a été enregistré."
                        },
                        onFailure = { e -> errorMessage = "Export : ${e.message ?: "erreur"}" },
                    )
                },
                onFailure = { e -> errorMessage = "Export : ${e.message ?: "erreur"}" },
            )
        }
    }

    val importPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText()
            } ?: error("Impossible de lire le fichier.")
        }.fold(
            onSuccess = { text ->
                vm.hasAnyStoredData { hasData ->
                    pendingImportJson = text
                    importHasExistingData = hasData
                    confirmReplaceImport = false
                    importScope = null
                }
            },
            onFailure = { e -> errorMessage = "Import : ${e.message ?: "erreur"}" },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Données", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Exporter, importer ou supprimer vos fiches exercices, modèles et séances (fichier JSON pour l’export et l’import).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val name =
                                "TrackFit-backup-${FrenchDateTime.formatBackupDay(System.currentTimeMillis())}.json"
                            exportLauncher.launch(name)
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Exporter") }
                    OutlinedButton(
                        onClick = {
                            importPicker.launch(arrayOf("application/json", "application/*", "*/*"))
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Importer") }
                }
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) { Text("Supprimer les données") }
                errorMessage?.let { msg ->
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    successDialogText?.let { text ->
        AlertDialog(
            onDismissRequest = { successDialogText = null },
            title = { Text("Opération réussie") },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { successDialogText = null }) { Text("OK") }
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer toutes les données ?") },
            text = {
                Text(
                    "Toutes les séances, modèles et fiches exercices seront effacés de cet appareil. " +
                        "Cette action est irréversible. Pensez à exporter avant si vous souhaitez une copie.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        vm.clearAllLocalData { result ->
                            result.fold(
                                onSuccess = {
                                    successDialogText =
                                        "Toutes les données ont été supprimées de cet appareil."
                                },
                                onFailure = { e ->
                                    errorMessage = "Suppression : ${e.message ?: "erreur"}"
                                },
                            )
                        }
                    },
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
            },
        )
    }

    pendingImportJson?.let { json ->
        fun dismissImport() {
            pendingImportJson = null
            confirmReplaceImport = false
            importScope = null
        }

        fun runImport(mode: DataImportMode, scope: ImportContentScope) {
            vm.importDataJson(json, mode, scope) { result ->
                result.fold(
                    onSuccess = { r ->
                        val reuseNote =
                            if (r.blueprintsReusedExisting > 0) {
                                " (${r.blueprintsReusedExisting} fiche(s) déjà en bibliothèque, sans doublon)"
                            } else {
                                ""
                            }
                        successDialogText =
                            "Import réussi : ${r.blueprints} exercice(s), ${r.templates} modèle(s), ${r.sessions} séance(s).$reuseNote"
                        dismissImport()
                    },
                    onFailure = { e ->
                        errorMessage = "Import : ${e.message ?: "erreur"}"
                        dismissImport()
                    },
                )
            }
        }

        when {
            importScope == null -> {
                AlertDialog(
                    onDismissRequest = { dismissImport() },
                    title = { Text("Que voulez-vous importer ?") },
                    text = {
                        Column(
                            modifier =
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            ImportContentScope.entries.forEach { option ->
                                val selected = scopeDraft == option
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { scopeDraft = option }
                                            .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { scopeDraft = option },
                                    )
                                    Column(Modifier.padding(start = 8.dp)) {
                                        Text(
                                            when (option) {
                                                ImportContentScope.ALL -> "Tout"
                                                ImportContentScope.EXERCISES_ONLY -> "Exercices uniquement"
                                                ImportContentScope.TEMPLATES_AND_EXERCISES -> "Modèles et exercices"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            when (option) {
                                                ImportContentScope.ALL ->
                                                    "Fiches exercices, modèles de séance et séances enregistrées."
                                                ImportContentScope.EXERCISES_ONLY ->
                                                    "Uniquement la bibliothèque de fiches exercices."
                                                ImportContentScope.TEMPLATES_AND_EXERCISES ->
                                                    "Modèles et leurs lignes ; uniquement les fiches exercices " +
                                                        "nécessaires aux modèles (pas les séances)."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (!importHasExistingData) {
                                    runImport(DataImportMode.MERGE, scopeDraft)
                                } else {
                                    importScope = scopeDraft
                                }
                            },
                        ) { Text("Continuer") }
                    },
                    dismissButton = {
                        TextButton(onClick = { dismissImport() }) { Text("Annuler") }
                    },
                )
            }
            confirmReplaceImport -> {
                AlertDialog(
                    onDismissRequest = { dismissImport() },
                    title = { Text("Remplacer toutes les données ?") },
                    text = {
                        Text(
                            "Toutes les données locales (séances, modèles, exercices) seront supprimées avant l’import.",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                importScope?.let { scope ->
                                    runImport(DataImportMode.REPLACE, scope)
                                }
                            },
                        ) { Text("Remplacer et importer") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { confirmReplaceImport = false },
                        ) { Text("Retour") }
                    },
                )
            }
            else -> {
                AlertDialog(
                    onDismissRequest = { dismissImport() },
                    title = { Text("Importer les données") },
                    text = {
                        Text(
                            "Fusionner : ajoute le contenu du fichier à vos données actuelles.\n\n" +
                                "Remplacer tout : efface d’abord toutes les données de l’app, puis importe selon le périmètre choisi.",
                        )
                    },
                    confirmButton = {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    importScope?.let { scope ->
                                        runImport(DataImportMode.MERGE, scope)
                                    }
                                },
                            ) { Text("Fusionner") }
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { confirmReplaceImport = true },
                            ) { Text("Remplacer tout…") }
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { dismissImport() },
                            ) { Text("Annuler") }
                        }
                    },
                )
            }
        }
    }
}
