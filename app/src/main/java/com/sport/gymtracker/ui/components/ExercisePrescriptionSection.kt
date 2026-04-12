package com.sport.gymtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.domain.ExerciseDurationTimeUnit
import com.sport.gymtracker.domain.ExerciseWorkMode
import com.sport.gymtracker.domain.showsSetsInEditor

private const val ChargeOuNiveauHint =
    "Une valeur (ex. 60), plusieurs (ex. 60, 65, 70) ou un intervalle (ex. 8-12). Unité libre (kg, %, niveau…)."

@Composable
private fun ModeDescription(workMode: ExerciseWorkMode) {
    val text = when (workMode) {
        ExerciseWorkMode.REPS_LOAD ->
            "Séries en répétitions ; charge et réglage machine (cran, siège, etc.) optionnels."
        ExerciseWorkMode.TIME_DURATION ->
            "Choisis secondes ou minutes. Le nombre de séries et le réglage machine sont optionnels (une série si tu laisses le nombre vide)."
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePrescriptionSection(
    workMode: ExerciseWorkMode,
    onWorkModeChange: (ExerciseWorkMode) -> Unit,
    durationTimeUnit: ExerciseDurationTimeUnit,
    onDurationTimeUnitChange: (ExerciseDurationTimeUnit) -> Unit,
    sets: String,
    onSetsChange: (String) -> Unit,
    reps: String,
    onRepsChange: (String) -> Unit,
    durationSec: String,
    onDurationSecChange: (String) -> Unit,
    durationMin: String,
    onDurationMinChange: (String) -> Unit,
    levelOrReglage: String,
    onLevelOrReglageChange: (String) -> Unit,
    loadSpecStr: String,
    onLoadSpecChange: (String) -> Unit,
    chargeLabel: String = "Charge",
    chargeOptional: Boolean = true,
) {
    var modeMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExposedDropdownMenuBox(
            expanded = modeMenu,
            onExpandedChange = { modeMenu = it },
        ) {
            OutlinedTextField(
                value = workMode.labelFr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type d’exercice") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeMenu) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )
            DropdownMenu(
                expanded = modeMenu,
                onDismissRequest = { modeMenu = false },
            ) {
                ExerciseWorkMode.entries.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.labelFr) },
                        onClick = {
                            onWorkModeChange(m)
                            modeMenu = false
                        },
                    )
                }
            }
        }

        ModeDescription(workMode)

               if (workMode.showsSetsInEditor()) {
            OutlinedTextField(
                value = sets,
                onValueChange = { onSetsChange(it.filter { c -> c.isDigit() }) },
                label = { Text("Nombre de séries") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        when (workMode) {
            ExerciseWorkMode.REPS_LOAD -> {
                OutlinedTextField(
                    value = reps,
                    onValueChange = { onRepsChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("Répétitions par série") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = loadSpecStr,
                    onValueChange = onLoadSpecChange,
                    label = {
                        Text(
                            if (chargeOptional) "$chargeLabel (optionnel)"
                            else chargeLabel,
                        )
                    },
                    placeholder = { Text("Ex. 20 · 20, 22, 25 · 8-12 kg") },
                    supportingText = { Text(ChargeOuNiveauHint) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = levelOrReglage,
                    onValueChange = onLevelOrReglageChange,
                    label = { Text("Niveau ou réglage (optionnel)") },
                    placeholder = { Text("Ex. cran 5 · hauteur siège 3 · position P2") },
                    supportingText = {
                        Text("Réglage machine, siège, poulie, chariot… Indépendant de la charge.")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            ExerciseWorkMode.TIME_DURATION -> {
                OutlinedTextField(
                    value = if (durationTimeUnit == ExerciseDurationTimeUnit.SECONDS) durationSec else durationMin,
                    onValueChange = { v ->
                        val f = v.filter { c -> c.isDigit() }
                        if (durationTimeUnit == ExerciseDurationTimeUnit.SECONDS) {
                            onDurationSecChange(f)
                        } else {
                            onDurationMinChange(f)
                        }
                    },
                    label = { Text("Durée") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = durationTimeUnit == ExerciseDurationTimeUnit.SECONDS,
                        onClick = { onDurationTimeUnitChange(ExerciseDurationTimeUnit.SECONDS) },
                        label = { Text("Secondes") },
                    )
                    FilterChip(
                        selected = durationTimeUnit == ExerciseDurationTimeUnit.MINUTES,
                        onClick = { onDurationTimeUnitChange(ExerciseDurationTimeUnit.MINUTES) },
                        label = { Text("Minutes") },
                    )
                }
                OutlinedTextField(
                    value = sets,
                    onValueChange = { onSetsChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("Nombre de séries (optionnel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = levelOrReglage,
                    onValueChange = onLevelOrReglageChange,
                    label = { Text("Niveau ou réglage (optionnel)") },
                    placeholder = { Text("Ex. cran 5 · hauteur siège 3 · position P2") },
                    supportingText = {
                        Text("Réglage machine, siège, poulie, chariot… Tu peux laisser vide.")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (durationTimeUnit == ExerciseDurationTimeUnit.SECONDS) {
                    OutlinedTextField(
                        value = loadSpecStr,
                        onValueChange = onLoadSpecChange,
                        label = {
                            Text(
                                if (chargeOptional) "$chargeLabel (optionnel)"
                                else chargeLabel,
                            )
                        },
                        placeholder = { Text("Ex. 10 · 12, 14 · 5-8 kg") },
                        supportingText = { Text(ChargeOuNiveauHint) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        }
    }
}
