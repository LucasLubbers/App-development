package com.example.workoutbuddyapplication.utils

import com.example.workoutbuddyapplication.ui.theme.UnitSystem
import kotlin.math.round

object UnitConverter {
    // Weight conversions
    private const val KG_TO_LBS = 2.20462
    private const val LBS_TO_KG = 0.453592

    // Distance conversions
    private const val KM_TO_MILES = 0.621371
    private const val MILES_TO_KM = 1.60934

    /**
     * Convert weight from kg to lbs
     */
    fun kgToLbs(kg: Double): Double {
        return round(kg * KG_TO_LBS * 10) / 10 // Round to 1 decimal place
    }

    /**
     * Convert weight from lbs to kg
     */
    fun lbsToKg(lbs: Double): Double {
        return round(lbs * LBS_TO_KG * 10) / 10 // Round to 1 decimal place
    }

    /**
     * Convert distance from km to miles
     */
    fun kmToMiles(km: Double): Double {
        return round(km * KM_TO_MILES * 100) / 100 // Round to 2 decimal places
    }

    /**
     * Convert distance from miles to km
     */
    fun milesToKm(miles: Double): Double {
        return round(miles * MILES_TO_KM * 100) / 100 // Round to 2 decimal places
    }

    /**
     * Format weight with appropriate unit
     */
    fun formatWeight(weightInKg: Double, unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "${weightInKg} kg"
            UnitSystem.IMPERIAL -> "${kgToLbs(weightInKg)} lbs"
        }
    }

    /**
     * Format distance with appropriate unit
     */
    fun formatDistance(distanceInKm: Double, unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> String.format("%.2f km", distanceInKm)
            UnitSystem.IMPERIAL -> String.format("%.2f mi", kmToMiles(distanceInKm))
        }
    }

    /**
     * Get weight unit string
     */
    fun getWeightUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "kg"
            UnitSystem.IMPERIAL -> "lbs"
        }
    }

    /**
     * Get distance unit string
     */
    fun getDistanceUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "km"
            UnitSystem.IMPERIAL -> "mi"
        }
    }

    /**
     * Convert weight input to kg for storage
     */
    fun weightToKg(weight: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.METRIC -> weight
            UnitSystem.IMPERIAL -> lbsToKg(weight)
        }
    }

    /**
     * Convert weight from kg storage to display
     */
    fun weightFromKg(weightInKg: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.METRIC -> weightInKg
            UnitSystem.IMPERIAL -> kgToLbs(weightInKg)
        }
    }

    /**
     * Convert distance input to km for storage
     */
    fun distanceToKm(distance: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.METRIC -> distance
            UnitSystem.IMPERIAL -> milesToKm(distance)
        }
    }

    /**
     * Convert distance from km storage to display
     */
    fun distanceFromKm(distanceInKm: Double, unitSystem: UnitSystem): Double {
        return when (unitSystem) {
            UnitSystem.METRIC -> distanceInKm
            UnitSystem.IMPERIAL -> kmToMiles(distanceInKm)
        }
    }

    /**
     * Convert weight from kg storage to display weight for user input
     */
    fun toDisplayWeight(weightInKg: Double, unitSystem: UnitSystem): Double {
        return weightFromKg(weightInKg, unitSystem)
    }

    /**
     * Convert weight from display input to kg for storage
     */
    fun fromDisplayWeight(displayWeight: Double, unitSystem: UnitSystem): Double {
        return weightToKg(displayWeight, unitSystem)
    }
} 