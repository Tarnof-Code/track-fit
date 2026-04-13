package com.sport.gymtracker.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private fun formatRestCountdown(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) String.format("%d:%02d", m, s) else "${s}s"
}

private suspend fun playRestFinishedSound() {
    var tone: ToneGenerator? = null
    try {
        tone = ToneGenerator(AudioManager.STREAM_ALARM, 85)
        repeat(2) {
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 220)
            delay(320)
        }
    } catch (_: Exception) {
    } finally {
        tone?.release()
    }
}

/**
 * Repos plein écran : bloque la navigation (retour = arrêt) jusqu’à la fin du décompte ou à l’arrêt manuel.
 */
@Composable
fun FullscreenRestCountdownOverlay(
    totalSeconds: Int,
    onFinished: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (totalSeconds <= 0) return

    var remaining by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(totalSeconds) {
        var r = totalSeconds
        remaining = r
        while (r > 0) {
            delay(1000)
            r--
            remaining = r
        }
        playRestFinishedSound()
        onFinished()
    }

    BackHandler { onStop() }

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Repos",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = formatRestCountdown(remaining),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onStop) {
                Text("Arrêter le chrono")
            }
        }
    }
}
