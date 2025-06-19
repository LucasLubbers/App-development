package com.example.workoutbuddyapplication.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.temporal.IsoFields

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YearHeatmap(
    year: Int,
    workoutDates: List<LocalDate>
) {
    val daysInYear = if (Year.isLeap(year.toLong())) 366 else 365
    val firstDay = LocalDate.of(year, 1, 1)
    val today = LocalDate.now()
    val workoutSet = workoutDates.toSet()

    val firstDayOfWeek = firstDay.dayOfWeek.value % 7

    val dayLabels = listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo")

    Column {
        Row {
            Spacer(modifier = Modifier.width(28.dp))
            dayLabels.forEach {
                Text(
                    it,
                    modifier = Modifier
                        .weight(1f)
                        .padding(1.dp),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        var dayCounter = 0
        for (week in 0 until 53) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val weekDate = firstDay.plusDays((week * 7 - firstDayOfWeek + 1).toLong()).with(
                    DayOfWeek.MONDAY
                )
                val weekNumber = weekDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                Text(
                    text = weekNumber.toString().padStart(2, '0'),
                    fontSize = 10.sp,
                    modifier = Modifier.width(28.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                for (day in 0..6) {
                    val gridIndex = week * 7 + day
                    val date = firstDay.plusDays((gridIndex - firstDayOfWeek + 1).toLong())
                    if (gridIndex >= firstDayOfWeek - 1 && dayCounter < daysInYear) {
                        val isWorkout = date in workoutSet
                        val isToday = date == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(
                                    color = when {
                                        isToday -> MaterialTheme.colorScheme.secondary
                                        isWorkout -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        dayCounter++
                    } else {
                        Spacer(modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f))
                    }
                }
            }
            if (dayCounter >= daysInYear) break
        }
    }
}