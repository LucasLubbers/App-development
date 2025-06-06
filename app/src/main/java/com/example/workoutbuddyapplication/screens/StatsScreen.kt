package com.example.workoutbuddyapplication.screens

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.models.Workout
import com.example.workoutbuddyapplication.models.WorkoutType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.workoutbuddyapplication.navigation.Screen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import kotlin.compareTo
import kotlin.div
import kotlin.text.toDouble
import kotlin.text.toLong

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(3) }
    var selectedTimeRange by remember { mutableStateOf(0) }
    val timeRanges = listOf("Week", "Maand", "Jaar")

    val context = LocalContext.current
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch workouts on load
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        val userId = getUserId(context)
        if (userId != null) {
            workouts = fetchWorkouts(userId)
        } else {
            error = "User not logged in."
        }
        isLoading = false
    }

    // Calculate stats
    val totalWorkouts = workouts.size
    val totalDurationMinutes = workouts.sumOf { it.duration }
    val totalDurationHours = totalDurationMinutes / 60.0
    val totalDistance = workouts.mapNotNull { it.distance }.sum()

    // For average per week (for month view)
    val now = LocalDate.now()
    val startOfWeek = now.minusDays((now.dayOfWeek.value - 1).toLong())
    val endOfWeek = startOfWeek.plusDays(6)
    val currentMonthWorkouts = workouts.filter {
        val date = LocalDate.parse(it.date)
        date.year == now.year && date.month == now.month
    }

    val currentWeekWorkouts = workouts.filter {
        val date = LocalDate.parse(it.date)
        !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
    }

    val avgPerWeekInMonth = if (currentMonthWorkouts.isNotEmpty()) {
        val first = currentMonthWorkouts.minOf { LocalDate.parse(it.date) }
        val last = currentMonthWorkouts.maxOf { LocalDate.parse(it.date) }
        val weeks = ChronoUnit.WEEKS.between(first, last).toInt() + 1
        (currentMonthWorkouts.size.toDouble() / weeks).takeIf { weeks > 0 } ?: 0.0
    } else 0.0

    val avgDurationPerWorkoutWeek = if (currentWeekWorkouts.isNotEmpty()) {
        currentWeekWorkouts.sumOf { it.duration }.toDouble() / currentWeekWorkouts.size
    } else 0.0

    val currentYearWorkouts = workouts.filter { LocalDate.parse(it.date).year == now.year }
    val monthsPassed = if (now.monthValue > 0) now.monthValue else 1
    val avgWorkoutsPerMonth = if (monthsPassed > 0) {
        currentYearWorkouts.size.toDouble() / monthsPassed
    } else 0.0

    val filteredWorkouts = when (selectedTimeRange) {
        0 -> currentWeekWorkouts
        1 -> currentMonthWorkouts
        else -> workouts
    }

    // Distribution by type
    val running = filteredWorkouts.count { it.workoutTypeEnum == WorkoutType.RUNNING }
    val cycling = filteredWorkouts.count { it.workoutTypeEnum == WorkoutType.CYCLING }
    val strength = filteredWorkouts.count { it.workoutTypeEnum == WorkoutType.STRENGTH }
    val yoga = filteredWorkouts.count { it.workoutTypeEnum == WorkoutType.YOGA }
    val other = filteredWorkouts.count { it.workoutTypeEnum == WorkoutType.OTHER }
    val totalForDist = running + cycling + strength + yoga + other
    fun percent(count: Int) = if (totalForDist > 0) (count * 100 / totalForDist) else 0

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistieken",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { navController.navigate(Screen.Goals.route) }
                ) {
                    Text("Doelen Bekijken")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time range tabs
            TabRow(selectedTabIndex = selectedTimeRange) {
                timeRanges.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTimeRange == index,
                        onClick = { selectedTimeRange = index },
                        text = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                else -> {
                    StatsSummaryCards(
                        totalWorkouts = when (selectedTimeRange) {
                            0 -> currentWeekWorkouts.size
                            1 -> currentMonthWorkouts.size
                            else -> currentYearWorkouts.size
                        },
                        totalDurationHours = when (selectedTimeRange) {
                            0 -> currentWeekWorkouts.sumOf { it.duration } / 60.0
                            1 -> currentMonthWorkouts.sumOf { it.duration } / 60.0
                            else -> currentYearWorkouts.sumOf { it.duration } / 60.0
                        },
                        totalDistance = when (selectedTimeRange) {
                            0 -> currentWeekWorkouts.mapNotNull { it.distance }.sum()
                            1 -> currentMonthWorkouts.mapNotNull { it.distance }.sum()
                            else -> currentYearWorkouts.mapNotNull { it.distance }.sum()
                        },
                        avgPerWeek = avgPerWeekInMonth,
                        showAvgPerWeek = selectedTimeRange == 1,
                        avgDurationPerWorkoutWeek = avgDurationPerWorkoutWeek,
                        showAvgDurationPerWorkoutWeek = selectedTimeRange == 0,
                        avgWorkoutsPerMonth = avgWorkoutsPerMonth,
                        showAvgWorkoutsPerMonth = selectedTimeRange == 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Workout Activiteit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTimeRange) {
                        0 -> { // Week
                            WorkoutActivityChart(currentWeekWorkouts)
                        }
                        1 -> { // Maand
                            CalendarMonthViewStyled(
                                year = now.year,
                                month = now.monthValue,
                                workoutDays = currentMonthWorkouts.map { LocalDate.parse(it.date).dayOfMonth }
                            )
                        }
                        2 -> { // Jaar
                            YearHeatmap(
                                year = now.year,
                                workoutDates = workouts.map { LocalDate.parse(it.date) }.filter { it.year == now.year }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Verdeling per type",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WorkoutTypeDistribution(
                        running = percent(running),
                        cycling = percent(cycling),
                        strength = percent(strength),
                        yoga = percent(yoga),
                        other = percent(other)
                    )
                }
            }
        }
    }
}

// Styled calendar grid for the month, marking workout days and today
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarMonthViewStyled(year: Int, month: Int, workoutDays: List<Int>) {
    val today = LocalDate.now()
    val firstDay = LocalDate.of(year, month, 1)
    val daysInMonth = firstDay.lengthOfMonth()
    val firstDayOfWeek = (firstDay.dayOfWeek.value + 6) % 7 // Monday=0, Sunday=6
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
                        val isToday = today.year == year && today.monthValue == month && today.dayOfMonth == dayNum
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
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

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

    // Monday = 1, Sunday = 7
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 0=Sunday, 1=Monday, ..., 6=Saturday

    // Day labels (Monday to Sunday)
    val dayLabels = listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo")

    Column {
        Row {
            Spacer(modifier = Modifier.width(28.dp)) // Space for week numbers
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
                // Week number (ISO-8601)
                val weekDate = firstDay.plusDays((week * 7 - firstDayOfWeek + 1).toLong()).with(
                    DayOfWeek.MONDAY)
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
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
            if (dayCounter >= daysInYear) break
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun WorkoutActivityChart(workouts: List<Workout>) {
    val days = listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo")
    val counts = IntArray(7)
    workouts.forEach {
        val dayOfWeek = LocalDate.parse(it.date).dayOfWeek.value - 1 // 0=Ma, 6=Zo
        counts[dayOfWeek]++
    }
    val max = counts.maxOrNull()?.takeIf { it > 0 } ?: 1
    val values = counts.map { it.toFloat() / max }

    val textColor = MaterialTheme.colorScheme.onSurface
    val colorScheme = MaterialTheme.colorScheme

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
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
        StatsSummaryCard("Totale Tijd", String.format("%.1f", totalDurationHours), "uren", Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatsSummaryCard("Afstand", String.format("%.1f", totalDistance), "km", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        when {
            showAvgPerWeek -> StatsSummaryCard("Gemiddeld", String.format("%.1f", avgPerWeek), "per week", Modifier.weight(1f))
            showAvgDurationPerWorkoutWeek -> StatsSummaryCard("Gem. duur", String.format("%.0f", avgDurationPerWorkoutWeek), "min/wo", Modifier.weight(1f))
            showAvgWorkoutsPerMonth -> StatsSummaryCard("Gemiddeld", String.format("%.1f", avgWorkoutsPerMonth), "per maand", Modifier.weight(1f))
            else -> Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatsSummaryCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 20.sp)
            Text(subtitle, color = Color.Gray)
        }
    }
}

@Composable
fun WorkoutTypeDistribution(running: Int, cycling: Int, strength: Int, yoga: Int, other: Int) {
    Column {
        WorkoutTypeBar("Hardlopen", running, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Fietsen", cycling, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Krachttraining", strength, MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Yoga", yoga, MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar("Overig", other, MaterialTheme.colorScheme.surfaceVariant)
    }
}

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

        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
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