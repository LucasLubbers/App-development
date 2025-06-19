package com.example.workoutbuddyapplication.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WorkoutTypeBar(type: String, percentage: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(type)
            Text("$percentage%")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)) {
            val width = size.width * percentage / 100

            drawLine(
                color = Color.LightGray,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = size.height
            )

            drawLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(width, size.height / 2),
                strokeWidth = size.height
            )
        }
    }
}