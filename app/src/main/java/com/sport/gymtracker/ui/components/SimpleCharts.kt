package com.sport.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sport.gymtracker.data.MuscleFrequencyItem

@Composable
fun MuscleFrequencyBars(
    items: List<MuscleFrequencyItem>,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        Text(
            text = "Aucun muscle renseigné sur les séances terminées.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEach { item ->
            val barColor = colorForMuscleStorageKey(item.muscleKey)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = item.labelFr,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.widthIn(max = 128.dp).weight(0.42f),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(item.fractionOfMax.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(barColor),
                    )
                }
                Text(
                    text = "${item.count}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
