package com.example.workoutbuddyapplication.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.navigation.Screen
import kotlinx.coroutines.delay

data class YogaPose(
    val name: String,
    val duration: Int, // in seconds
    val description: String,
    val difficulty: String = "Beginner", // Beginner, Intermediate, Advanced
    val benefits: String = "",
    val breathingPattern: String = "Normale ademhaling"
)

data class YogaRoutine(
    val name: String,
    val description: String,
    val poses: List<YogaPose>,
    val duration: Int, // Total duration in minutes
    val level: String // Beginner, Intermediate, Advanced
)

@Composable
fun YogaWorkoutScreen(navController: NavController) {
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var calories by remember { mutableIntStateOf(0) }

    // Yoga routines
    val yogaRoutines = remember {
        mutableStateListOf(
            YogaRoutine(
                name = "Ochtend Flow",
                description = "Een zachte routine om de dag te beginnen",
                poses = listOf(
                    YogaPose(
                        name = "Berghouding (Tadasana)",
                        duration = 60,
                        description = "Sta rechtop met je voeten samen, armen langs je lichaam.",
                        benefits = "Verbetert houding, balans en lichaamsbesef"
                    ),
                    YogaPose(
                        name = "Neerwaartse Hond (Adho Mukha Svanasana)",
                        duration = 90,
                        description = "Vorm een omgekeerde V met je lichaam, handen en voeten op de grond.",
                        benefits = "Strekt de rug, versterkt armen en schouders"
                    ),
                    YogaPose(
                        name = "Krijger I (Virabhadrasana I)",
                        duration = 60,
                        description = "Longe met één been naar voren, armen omhoog gestrekt.",
                        difficulty = "Intermediate",
                        benefits = "Versterkt benen, verbetert balans en focus"
                    ),
                    YogaPose(
                        name = "Krijger II (Virabhadrasana II)",
                        duration = 60,
                        description = "Longe met één been naar voren, armen gestrekt naar de zijkanten.",
                        difficulty = "Intermediate",
                        benefits = "Opent heupen, versterkt benen"
                    ),
                    YogaPose(
                        name = "Kindhouding (Balasana)",
                        duration = 120,
                        description = "Kniel met je voorhoofd op de grond, armen langs je lichaam of gestrekt.",
                        benefits = "Ontspant de rug, kalmeert de geest"
                    )
                ),
                duration = 15,
                level = "Beginner"
            ),
            YogaRoutine(
                name = "Kracht & Balans",
                description = "Focus op kracht en balans poses",
                poses = listOf(
                    YogaPose(
                        name = "Boom (Vrksasana)",
                        duration = 45,
                        description = "Sta op één been, plaats de voet van het andere been tegen de binnenkant van je dij.",
                        difficulty = "Intermediate",
                        benefits = "Verbetert balans en concentratie"
                    ),
                    YogaPose(
                        name = "Stoel (Utkatasana)",
                        duration = 60,
                        description = "Buig je knieën alsof je op een stoel zit, armen omhoog.",
                        benefits = "Versterkt benen en core"
                    ),
                    YogaPose(
                        name = "Plank (Phalakasana)",
                        duration = 45,
                        description = "Houd je lichaam in een rechte lijn, steunend op handen en tenen.",
                        difficulty = "Intermediate",
                        benefits = "Versterkt core, armen en schouders",
                        breathingPattern = "Diepe, gelijkmatige ademhaling"
                    ),
                    YogaPose(
                        name = "Zijwaartse Plank (Vasisthasana)",
                        duration = 30,
                        description = "Draai vanuit plank naar één zijde, steunend op één hand en de zijkant van je voeten.",
                        difficulty = "Advanced",
                        benefits = "Versterkt armen, schouders en core"
                    ),
                    YogaPose(
                        name = "Adelaar (Garudasana)",
                        duration = 45,
                        description = "Kruis je armen en benen voor elkaar in een gedraaide positie.",
                        difficulty = "Intermediate",
                        benefits = "Verbetert balans, concentratie en coördinatie"
                    )
                ),
                duration = 20,
                level = "Intermediate"
            )
        )
    }

    var selectedRoutineIndex by remember { mutableIntStateOf(0) }
    var currentPoseIndex by remember { mutableIntStateOf(0) }
    var poseTimeRemaining by remember { mutableIntStateOf(yogaRoutines[selectedRoutineIndex].poses[0].duration) }

    var showAddRoutine by remember { mutableStateOf(false) }
    var showRoutineSelector by remember { mutableStateOf(false) }
    var showAddPose by remember { mutableStateOf(false) }

    // New routine fields
    var newRoutineName by remember { mutableStateOf("") }
    var newRoutineDescription by remember { mutableStateOf("") }
    var newRoutineLevel by remember { mutableStateOf("Beginner") }

    // New pose fields
    var newPoseName by remember { mutableStateOf("") }
    var newPoseDuration by remember { mutableIntStateOf(60) }
    var newPoseDescription by remember { mutableStateOf("") }
    var newPoseDifficulty by remember { mutableStateOf("Beginner") }
    var newPoseBenefits by remember { mutableStateOf("") }

    // Timer effect for overall workout time
    LaunchedEffect(isRunning) {
        val startTime = SystemClock.elapsedRealtime() - elapsedTime
        while (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime
            delay(1000)

            // Simulate calorie burn (about 3 calories per minute for yoga)
            if (isRunning) {
                calories = (elapsedTime / 60000 * 3).toInt()
            }
        }
    }

    // Timer effect for pose timing
    LaunchedEffect(isRunning, currentPoseIndex) {
        val currentRoutine = yogaRoutines[selectedRoutineIndex]
        if (currentPoseIndex < currentRoutine.poses.size) {
            poseTimeRemaining = currentRoutine.poses[currentPoseIndex].duration

            while (isRunning && poseTimeRemaining > 0) {
                delay(1000)
                poseTimeRemaining--
            }

            if (poseTimeRemaining <= 0 && currentPoseIndex < currentRoutine.poses.size - 1) {
                currentPoseIndex++
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pauzeren" else "Hervatten"
                    )
                }

                FloatingActionButton(
                    onClick = { navController.navigate(Screen.WorkoutCompleted.route) },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stoppen"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SelfImprovement,
                        contentDescription = "Yoga",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Yoga",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = yogaRoutines[selectedRoutineIndex].name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { showRoutineSelector = true }
                ) {
                    Text("Routine wijzigen")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Tijd",
                    value = formatTime(elapsedTime),
                    icon = Icons.Default.Edit,
                    modifier = Modifier.weight(1f)
                )


                Spacer(modifier = Modifier.padding(4.dp))

                StatCard(
                    title = "Calorieën",
                    value = "$calories kcal",
                    icon = Icons.Default.Edit,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current pose
            val currentRoutine = yogaRoutines[selectedRoutineIndex]
            if (currentPoseIndex < currentRoutine.poses.size) {
                val currentPose = currentRoutine.poses[currentPoseIndex]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Huidige Pose",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Moeilijkheid: ${currentPose.difficulty}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentPose.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Timer
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = poseTimeRemaining.toFloat() / currentPose.duration.toFloat(),
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 8.dp
                                )

                                Text(
                                    text = "$poseTimeRemaining",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Pose image placeholder
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SelfImprovement,
                                    contentDescription = "Pose illustratie",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentPose.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (currentPose.benefits.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Voordelen: ${currentPose.benefits}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ademhaling: ${currentPose.breathingPattern}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (currentPoseIndex > 0) {
                                OutlinedButton(
                                    onClick = {
                                        currentPoseIndex--
                                        poseTimeRemaining = currentRoutine.poses[currentPoseIndex].duration
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Vorige pose",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Vorige")
                                }
                            }

                            if (currentPoseIndex < currentRoutine.poses.size - 1) {
                                Button(
                                    onClick = {
                                        currentPoseIndex++
                                        poseTimeRemaining = currentRoutine.poses[currentPoseIndex].duration
                                    }
                                ) {
                                    Text("Volgende")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.SkipNext,
                                        contentDescription = "Volgende pose",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming poses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volgende Poses",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                if (!showAddRoutine && !showRoutineSelector) {
                    IconButton(onClick = { showAddPose = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Pose toevoegen")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overall progress
            LinearProgressIndicator(
                progress = (currentPoseIndex.toFloat() + 1) / currentRoutine.poses.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (showRoutineSelector) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Kies een Yoga Routine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(yogaRoutines) { routine ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = routine.name,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = "${routine.duration} min | ${routine.level}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            selectedRoutineIndex = yogaRoutines.indexOf(routine)
                                            currentPoseIndex = 0
                                            poseTimeRemaining = routine.poses[0].duration
                                            showRoutineSelector = false
                                        }
                                    ) {
                                        Text("Selecteren")
                                    }
                                }
                            }

                            item {
                                OutlinedButton(
                                    onClick = {
                                        showRoutineSelector = false
                                        showAddRoutine = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Nieuwe routine")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Nieuwe Routine Maken")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showRoutineSelector = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sluiten")
                        }
                    }
                }
            } else if (showAddRoutine) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Nieuwe Yoga Routine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newRoutineName,
                            onValueChange = { newRoutineName = it },
                            label = { Text("Routine naam") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newRoutineDescription,
                            onValueChange = { newRoutineDescription = it },
                            label = { Text("Beschrijving") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Niveau")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                                FilterChip(
                                    label = level,
                                    isSelected = newRoutineLevel == level,
                                    onClick = { newRoutineLevel = level }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showAddRoutine = false }
                            ) {
                                Text("Annuleren")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (newRoutineName.isNotBlank()) {
                                        yogaRoutines.add(
                                            YogaRoutine(
                                                name = newRoutineName,
                                                description = newRoutineDescription,
                                                poses = listOf(
                                                    YogaPose(
                                                        name = "Berghouding (Tadasana)",
                                                        duration = 60,
                                                        description = "Sta rechtop met je voeten samen, armen langs je lichaam."
                                                    )
                                                ),
                                                duration = 5,
                                                level = newRoutineLevel
                                            )
                                        )
                                        selectedRoutineIndex = yogaRoutines.size - 1
                                        currentPoseIndex = 0
                                        poseTimeRemaining = 60
                                        newRoutineName = ""
                                        newRoutineDescription = ""
                                        newRoutineLevel = "Beginner"
                                        showAddRoutine = false
                                    }
                                }
                            ) {
                                Text("Opslaan")
                            }
                        }
                    }
                }
            } else if (showAddPose) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Nieuwe Pose Toevoegen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPoseName,
                            onValueChange = { newPoseName = it },
                            label = { Text("Pose naam") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPoseDescription,
                            onValueChange = { newPoseDescription = it },
                            label = { Text("Beschrijving") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Moeilijkheid")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                                FilterChip(
                                    label = level,
                                    isSelected = newPoseDifficulty == level,
                                    onClick = { newPoseDifficulty = level }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Duur: ${newPoseDuration} seconden")

                        Slider(
                            value = newPoseDuration.toFloat(),
                            onValueChange = { newPoseDuration = it.toInt() },
                            valueRange = 10f..120f,
                            steps = 11
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPoseBenefits,
                            onValueChange = { newPoseBenefits = it },
                            label = { Text("Voordelen") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showAddPose = false }
                            ) {
                                Text("Annuleren")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (newPoseName.isNotBlank()) {
                                        val currentRoutine = yogaRoutines[selectedRoutineIndex]
                                        val updatedPoses = currentRoutine.poses.toMutableList()
                                        updatedPoses.add(
                                            YogaPose(
                                                name = newPoseName,
                                                duration = newPoseDuration,
                                                description = newPoseDescription,
                                                difficulty = newPoseDifficulty,
                                                benefits = newPoseBenefits
                                            )
                                        )

                                        yogaRoutines[selectedRoutineIndex] = currentRoutine.copy(
                                            poses = updatedPoses,
                                            duration = currentRoutine.duration + (newPoseDuration / 60)
                                        )

                                        newPoseName = ""
                                        newPoseDescription = ""
                                        newPoseDifficulty = "Beginner"
                                        newPoseDuration = 60
                                        newPoseBenefits = ""
                                        showAddPose = false
                                    }
                                }
                            ) {
                                Text("Toevoegen")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    val upcomingPoses = currentRoutine.poses.subList(
                        (currentPoseIndex + 1).coerceAtMost(currentRoutine.poses.size),
                        currentRoutine.poses.size
                    )

                    items(upcomingPoses) { pose ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pose.name,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "${pose.duration} sec | ${pose.difficulty}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                IconButton(
                                    onClick = { /* Edit pose */ }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Bewerken"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
