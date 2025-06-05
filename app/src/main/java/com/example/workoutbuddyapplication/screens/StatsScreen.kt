package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.R
import com.example.workoutbuddyapplication.components.BottomNavBar
import com.example.workoutbuddyapplication.navigation.Screen

@Composable
fun StatsScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(3) }
    var selectedTimeRange by remember { mutableStateOf(0) }
    val timeRanges = listOf(stringResource(R.string.week), stringResource(R.string.month), stringResource(R.string.year))

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
            Text(stringResource(R.string.statistics), fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = selectedTimeRange) {
                timeRanges.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTimeRange == index,
                        onClick = { selectedTimeRange = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatsSummaryCards()

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.workout_activity), fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WorkoutActivityChart()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.workout_distribution), fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WorkoutTypeDistribution(45, 30, 15, 10)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate(Screen.Goals.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_goals))
            }
        }
    }
}

@Composable
fun WorkoutActivityChart() {
    val values = listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.6f, 0.2f, 0.8f)
    val days = listOf(
        stringResource(R.string.monday).take(2),
        stringResource(R.string.tuesday).take(2),
        stringResource(R.string.wednesday).take(2),
        stringResource(R.string.thursday).take(2),
        stringResource(R.string.friday).take(2),
        stringResource(R.string.saturday).take(2),
        stringResource(R.string.sunday).take(2)
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val barWidth = size.width / (values.size * 1.5f)
        val spacing = barWidth / 2
        val maxBarHeight = size.height * 0.8f

        // Draw horizontal lines
        for (i in 0..4) {
            val y = size.height - (i * size.height / 4)
            drawLine(
                color = Color.LightGray,
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
                color = Color.Blue,
                start = Offset(x + barWidth / 2, size.height),
                end = Offset(x + barWidth / 2, y),
                strokeWidth = barWidth
            )

            // Draw text below bars
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    days[index],
                    x + barWidth / 2,
                    size.height + 30,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun StatsSummaryCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatsSummaryCard(stringResource(R.string.total), "23", stringResource(R.string.workouts), Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        StatsSummaryCard(stringResource(R.string.total_time), "15.5", stringResource(R.string.hours), Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatsSummaryCard(stringResource(R.string.distance), "87", "km", Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        StatsSummaryCard(stringResource(R.string.average), "3.2", stringResource(R.string.per_week), Modifier.weight(1f))
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
fun WorkoutTypeDistribution(running: Int, strength: Int, yoga: Int, other: Int) {
    Column {
        WorkoutTypeBar(stringResource(R.string.running), running, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar(stringResource(R.string.strength_training), strength, MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar(stringResource(R.string.yoga), yoga, MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(8.dp))
        WorkoutTypeBar(stringResource(R.string.other), other, MaterialTheme.colorScheme.surfaceVariant)
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

@Composable
fun Button(text: String, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
