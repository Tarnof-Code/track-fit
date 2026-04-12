package com.sport.gymtracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AddExerciseDropdownFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onNewExercise: () -> Unit,
    onFromLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        ExtendedFloatingActionButton(
            onClick = { onExpandedChange(true) },
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                )
            },
            text = { Text("Exercice") },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            DropdownMenuItem(
                text = { Text("Nouvel exercice") },
                onClick = {
                    onExpandedChange(false)
                    onNewExercise()
                },
            )
            DropdownMenuItem(
                text = { Text("Depuis la bibliothèque") },
                onClick = {
                    onExpandedChange(false)
                    onFromLibrary()
                },
            )
        }
    }
}
