package com.example.workoutbuddyapplication.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarMonthViewStyled(year: Int, month: Int, workoutDays: List<Int>) {
    val today = LocalDate.now()
    val firstDay = LocalDate.of(year, month, 1)
    val daysInMonth = firstDay.lengthOfMonth()
    val firstDayOfWeek = (firstDay.dayOfWeek.value + 6) % 7
    val rows = ((daysInMonth + firstDayOfWeek - 1) / 7) + 1

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo").forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0..6) {
                    val dayNum = row * 7 + col - (firstDayOfWeek - 1)
                    if (dayNum in 1..daysInMonth) {
                        val isWorkout = dayNum in workoutDays
                        val isToday =
                            today.year == year && today.monthValue == month && today.dayOfMonth == dayNum
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when {
                                            isToday -> MaterialTheme.colorScheme.secondary
                                            isWorkout -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        },
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    color = when {
                                        isToday || isWorkout -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f))
                    }
                }
            }
        }
    }
}