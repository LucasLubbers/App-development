package com.example.workoutbuddyapplication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsSummaryCards(
    totalWorkouts: Int,
    totalDurationHours: Double,
    totalDistance: Double,
    avgPerWeek: Double,
    showAvgPerWeek: Boolean,
    avgDurationPerWorkoutWeek: Double = 0.0,
    showAvgDurationPerWorkoutWeek: Boolean = false,
    avgWorkoutsPerMonth: Double = 0.0,
    showAvgWorkoutsPerMonth: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatsSummaryCard("Totaal", "$totalWorkouts", "workouts", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        StatsSummaryCard(
            "Totale Tijd",
            String.format("%.1f", totalDurationHours),
            "uren",
            Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatsSummaryCard("Afstand", String.format("%.1f", totalDistance), "km", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        when {
            showAvgPerWeek -> StatsSummaryCard(
                "Gemiddeld",
                String.format("%.1f", avgPerWeek),
                "per week",
                Modifier.weight(1f)
            )

            showAvgDurationPerWorkoutWeek -> StatsSummaryCard(
                "Gem. duur",
                String.format("%.0f", avgDurationPerWorkoutWeek),
                "min/wo",
                Modifier.weight(1f)
            )

            showAvgWorkoutsPerMonth -> StatsSummaryCard(
                "Gemiddeld",
                String.format("%.1f", avgWorkoutsPerMonth),
                "per maand",
                Modifier.weight(1f)
            )

            else -> Spacer(modifier = Modifier.weight(1f))
        }
    }
}