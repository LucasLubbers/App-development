import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector


data class Workout(
    val id: Int,
    val type: WorkoutType,
    val date: java.time.LocalDate,
    val duration: Int,
    val distance: Double?,
    val notes: String?
)

enum class WorkoutType(val displayName: String, val icon: ImageVector) {
    RUNNING("Hardlopen", Icons.AutoMirrored.Filled.DirectionsRun),
    CYCLING("Fietsen", Icons.AutoMirrored.Filled.DirectionsBike),
    STRENGTH("Krachttraining", Icons.Default.FitnessCenter),
    YOGA("Yoga", Icons.Default.SelfImprovement),
    OTHER("Overig", Icons.AutoMirrored.Filled.Help);

    companion object {
        fun fromString(value: String): WorkoutType =
            values().find {
                it.name.equals(value, ignoreCase = true) ||
                        it.displayName.equals(value, ignoreCase = true)
            } ?: OTHER
    }
}