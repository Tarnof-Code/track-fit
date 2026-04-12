package com.sport.gymtracker.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Courbe simple (Compose Canvas) : abscisses = libellés (ex. dates), ordonnées = valeurs numériques.
 */
@Composable
fun ProgressLineChart(
    points: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    if (points.isEmpty()) {
        Text(
            text = "Pas de point à afficher pour cette métrique.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val density = LocalDensity.current
    val labelSp = 10.sp
    val labelPx = remember(density) { with(density) { labelSp.toPx() } }
    val values = points.map { it.second }
    var yMin = values.minOrNull() ?: 0f
    var yMax = values.maxOrNull() ?: 1f
    if (yMin == yMax) {
        yMin -= 1f
        yMax += 1f
    }
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val axisLight = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        val w = size.width
        val h = size.height
        val padL = 44f
        val padR = 10f
        val padT = 8f
        val padB = 40f
        val n = points.size
        val plotW = (w - padL - padR).coerceAtLeast(1f)
        val plotH = (h - padT - padB).coerceAtLeast(1f)

        fun xAt(i: Int): Float =
            if (n <= 1) padL + plotW / 2f else padL + plotW * i / (n - 1).toFloat()

        fun yAt(v: Float): Float {
            val t = (v - yMin) / (yMax - yMin)
            return padT + plotH * (1f - t.coerceIn(0f, 1f))
        }

        drawLine(axisLight, Offset(padL, padT + plotH), Offset(padL + plotW, padT + plotH), strokeWidth = 1f)
        drawLine(axisLight, Offset(padL, padT), Offset(padL, padT + plotH), strokeWidth = 1f)

        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = labelPx
            color = labelColor
        }
        textPaint.textAlign = Paint.Align.RIGHT
        drawContext.canvas.nativeCanvas.drawText(
            formatYTick(yMax),
            padL - 6f,
            padT + labelPx * 0.35f,
            textPaint,
        )
        drawContext.canvas.nativeCanvas.drawText(
            formatYTick(yMin),
            padL - 6f,
            padT + plotH,
            textPaint,
        )

        textPaint.textAlign = Paint.Align.CENTER
        points.forEachIndexed { i, (label, _) ->
            val x = xAt(i)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                h - 6f,
                textPaint,
            )
        }

        val path = Path()
        points.forEachIndexed { i, (_, v) ->
            val x = xAt(i)
            val y = yAt(v)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path,
            color = lineColor,
            style = Stroke(width = 3f, cap = StrokeCap.Round),
        )
        points.forEachIndexed { i, (_, v) ->
            drawCircle(lineColor, 5f, Offset(xAt(i), yAt(v)))
            drawCircle(surfaceColor, 2f, Offset(xAt(i), yAt(v)))
        }
    }
}

private fun formatYTick(v: Float): String =
    if (v == v.toInt().toFloat()) v.toInt().toString() else "%.1f".format(v)
