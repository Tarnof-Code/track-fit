package com.sport.gymtracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.domain.OnAirLaDefenseCatalog

/** Hauteur max du panneau déroulant (défaut Material ~½ écran ; ici plus grand, contenu défilable). */
private val DropdownMaxHeight = 560.dp

/**
 * Saisie du matériel avec suggestions **ON AIR La Défense** (liste complète si le champ est vide, filtrage sinon).
 * Le texte final est celui enregistré (sélection ou saisie libre).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogEquipmentSelector(
    equipmentValue: String,
    onEquipmentValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val filtered = remember(equipmentValue) {
        val q = equipmentValue.trim()
        if (q.isEmpty()) {
            OnAirLaDefenseCatalog.items
        } else {
            OnAirLaDefenseCatalog.items.filter {
                it.label.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { open ->
            if (open) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
            }
            expanded = open
        },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = equipmentValue,
            onValueChange = {
                onEquipmentValueChange(it)
                expanded = true
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("Matériel") },
            placeholder = { Text("Tape pour filtrer ou saisir…") },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (equipmentValue.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                onEquipmentValueChange("")
                                expanded = false
                            },
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Effacer le matériel",
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = DropdownMaxHeight),
        ) {
            filtered.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${item.category} — ${item.label}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        keyboardController?.hide()
                        onEquipmentValueChange(item.label)
                        expanded = false
                    },
                )
            }
        }
    }
}
