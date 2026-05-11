package com.circadianx.sleepsense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.circadianx.sleepsense.ui.theme.GradientEnd
import com.circadianx.sleepsense.ui.theme.GradientStart
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme

@Composable
fun SsSparkline(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val colors = SleepSenseTheme.colors
    if (values.size < 2) return

    Canvas(modifier = modifier) {
        val maxVal = values.max().coerceAtLeast(1f)
        val step   = size.width / (values.size - 1).toFloat()

        val points = values.mapIndexed { i, v ->
            Offset(i * step, size.height - (v / maxVal) * size.height)
        }

        // Fill path
        val fillPath = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    GradientStart.copy(alpha = 0.3f),
                    GradientStart.copy(alpha = 0f)
                )
            )
        )

        // Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path  = linePath,
            brush = Brush.linearGradient(
                colors = listOf(GradientStart, GradientEnd),
                start  = Offset(0f, 0f),
                end    = Offset(size.width, 0f)
            ),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Last-point dot
        drawCircle(
            color  = colors.purple,
            radius = 4f,
            center = points.last()
        )
    }
}
