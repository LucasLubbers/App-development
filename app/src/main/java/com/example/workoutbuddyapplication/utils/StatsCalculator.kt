package com.example.workoutbuddyapplication.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StatsResult(
    val totalWorkouts: Int,
    val totalDurationMinutes: Int,
    val totalDistance: Double,
    val avgPerWeek: Double,
    val avgDurationPerWorkoutWeek: Double,
    val avgWorkoutsPerMonth: Double,
    val typeDistribution: Map<WorkoutType, Int>,
    val currentWeekWorkouts: List<Workout>,
    val currentMonthWorkouts: List<Workout>,
    val currentYearWorkouts: List<Workout>
)

object StatsCalculator {
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculate(workouts: List<Workout>, now: LocalDate): StatsResult {
        val totalWorkouts = workouts.size
        val totalDurationMinutes = workouts.sumOf { it.duration }
        val totalDistance = workouts.mapNotNull { it.distance }.sum()

        val startOfWeek = now.minusDays((now.dayOfWeek.value - 1).toLong())
        val endOfWeek = startOfWeek.plusDays(6)
        val currentWeekWorkouts = workouts.filter {
            val date = LocalDate.parse(it.date)
            !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
        }

        val currentMonthWorkouts = workouts.filter {
            val date = LocalDate.parse(it.date)
            date.year == now.year && date.month == now.month
        }

        val currentYearWorkouts = workouts.filter {
            LocalDate.parse(it.date).year == now.year
        }

        val avgPerWeek = if (currentMonthWorkouts.isNotEmpty()) {
            val first = currentMonthWorkouts.minOf { LocalDate.parse(it.date) }
            val last = currentMonthWorkouts.maxOf { LocalDate.parse(it.date) }
            val weeks = ChronoUnit.WEEKS.between(first, last).toInt() + 1
            (currentMonthWorkouts.size.toDouble() / weeks).takeIf { weeks > 0 } ?: 0.0
        } else 0.0

        val avgDurationPerWorkoutWeek = if (currentWeekWorkouts.isNotEmpty()) {
            currentWeekWorkouts.sumOf { it.duration }.toDouble() / currentWeekWorkouts.size
        } else 0.0

        val monthsPassed = if (now.monthValue > 0) now.monthValue else 1
        val avgWorkoutsPerMonth = if (monthsPassed > 0) {
            currentYearWorkouts.size.toDouble() / monthsPassed
        } else 0.0

        val typeDistribution = workouts.groupingBy { it.workoutTypeEnum }
            .eachCount()
            .withDefault { 0 }

        return StatsResult(
            totalWorkouts = totalWorkouts,
            totalDurationMinutes = totalDurationMinutes,
            totalDistance = totalDistance,
            avgPerWeek = avgPerWeek,
            avgDurationPerWorkoutWeek = avgDurationPerWorkoutWeek,
            avgWorkoutsPerMonth = avgWorkoutsPerMonth,
            typeDistribution = typeDistribution,
            currentWeekWorkouts = currentWeekWorkouts,
            currentMonthWorkouts = currentMonthWorkouts,
            currentYearWorkouts = currentYearWorkouts
        )
    }
}