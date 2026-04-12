package com.sport.gymtracker.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class RestPhase { Idle, Running, Finished }

private val RestFinishedGreen = Color(0xFFC8E6C9)
private val RestFinishedOnGreen = Color(0xFF1B5E20)

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
        // Pas de son sur certains appareils / modes silencieux partiels
    } finally {
        tone?.release()
    }
}

@Composable
fun RestBetweenSetsTimer(
    totalSeconds: Int,
    modifier: Modifier = Modifier,
) {
    if (totalSeconds <= 0) return

    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf(RestPhase.Idle) }
    var remaining by remember { mutableIntStateOf(totalSeconds) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        phase = RestPhase.Idle
        remaining = totalSeconds
    }

    fun startTimer() {
        if (phase != RestPhase.Idle) return
        phase = RestPhase.Running
        timerJob?.cancel()
        timerJob = scope.launch {
            try {
                var r = totalSeconds
                remaining = r
                while (r > 0) {
                    delay(1000)
                    r--
                    remaining = r
                }
                playRestFinishedSound()
                phase = RestPhase.Finished
                delay(5000)
                phase = RestPhase.Idle
                remaining = totalSeconds
            } finally {
                timerJob = null
            }
        }
    }

    LaunchedEffect(totalSeconds) {
        if (phase == RestPhase.Idle) {
            remaining = totalSeconds
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        when (phase) {
            RestPhase.Idle -> {
                val idleContainer = MaterialTheme.colorScheme.secondaryContainer
                val idleContent = MaterialTheme.colorScheme.onSecondaryContainer
                FilledTonalButton(
                    onClick = { startTimer() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = idleContainer,
                        contentColor = idleContent,
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Temps de repos : $totalSeconds sec",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Lancer le chrono",
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            RestPhase.Running -> {
                val runContainer = MaterialTheme.colorScheme.primaryContainer
                val runContent = MaterialTheme.colorScheme.onPrimaryContainer
                FilledTonalButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        disabledContainerColor = runContainer,
                        disabledContentColor = runContent,
                    ),
                ) {
                    Text(
                        text = "Repos en cours : ${formatRestCountdown(remaining)}",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                TextButton(
                    onClick = { cancelTimer() },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Annuler le chrono")
                }
            }
            RestPhase.Finished -> {
                FilledTonalButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        disabledContainerColor = RestFinishedGreen,
                        disabledContentColor = RestFinishedOnGreen,
                    ),
                ) {
                    Text(
                        text = "Temps écoulé",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
