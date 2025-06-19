package com.example.workoutbuddyapplication.components

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.workoutbuddyapplication.models.Workout
import java.time.LocalDate
import kotlin.collections.forEach

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun WorkoutActivityChart(workouts: List<Workout>) {
    val days = listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo")
    val counts = IntArray(7)
    workouts.forEach {
        val dayOfWeek = LocalDate.parse(it.date).dayOfWeek.value - 1
        counts[dayOfWeek]++
    }
    val max = counts.maxOrNull()?.takeIf { it > 0 } ?: 1
    val values = counts.map { it.toFloat() / max }

    val textColor = MaterialTheme.colorScheme.onSurface
    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)) {
        val barWidth = size.width / (values.size * 1.5f)
        val spacing = barWidth / 2
        val maxBarHeight = size.height * 0.8f

        for (i in 0..4) {
            val y = size.height - (i * size.height / 4)
            drawLine(
                color = colorScheme.surfaceVariant,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        values.forEachIndexed { index, value ->
            val x = index * (barWidth + spacing) + spacing
            val barHeight = maxBarHeight * value
            val y = size.height - barHeight

            drawLine(
                color = colorScheme.primary,
                start = Offset(x + barWidth / 2, size.height),
                end = Offset(x + barWidth / 2, y),
                strokeWidth = barWidth
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    color = textColor.toArgb()
                    textSize = 30f
                    textAlign = Paint.Align.CENTER
                }
                drawText(
                    days[index],
                    x + barWidth / 2,
                    size.height + 30,
                    paint
                )
            }
        }
    }
}