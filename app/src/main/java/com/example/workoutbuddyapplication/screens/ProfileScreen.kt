package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ProfileScreen(navController: NavController) {
    // Replace with actual user data
    val userName = "John Doe"
    val userEmail = "john.doe@example.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Profile",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            text = "Name: $userName",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Email: $userEmail",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        // Add more profile info or actions here
    }
}