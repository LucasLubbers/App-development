package com.example.workoutbuddyapplication.data

import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutExerciseWithDetails

interface WorkoutRepository {
    suspend fun getWorkoutById(workoutId: Int): Workout?
    suspend fun getExercisesForWorkout(workoutId: Int): List<WorkoutExerciseWithDetails>
}