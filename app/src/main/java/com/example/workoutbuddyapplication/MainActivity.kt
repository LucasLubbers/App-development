package com.example.workoutbuddyapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.workoutbuddyapplication.navigation.AppNavigation
import com.example.workoutbuddyapplication.navigation.DEBUG_MODE
import com.example.workoutbuddyapplication.ui.theme.WorkoutBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkoutBuddyTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        AppNavigation(navController = navController)
                    }
                    
                    // Debug indicator
                    if (DEBUG_MODE) {
                        DebugIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun DebugIndicator() {
    var showDebugIndicator by remember { mutableStateOf(true) }
    
    if (showDebugIndicator) {
        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 40.dp, end = 16.dp)
                    .background(Color(0xFF90EE90).copy(alpha = 0.7f))
                    .clickable { showDebugIndicator = false }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "DEBUG MODE",
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }
    }
}
