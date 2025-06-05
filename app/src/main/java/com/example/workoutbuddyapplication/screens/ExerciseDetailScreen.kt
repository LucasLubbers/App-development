package com.example.workoutbuddyapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.workoutbuddyapplication.models.Exercise
import com.example.workoutbuddyapplication.BuildConfig
import com.example.workoutbuddyapplication.components.BottomNavBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

suspend fun fetchExerciseByName(name: String): Exercise? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val url = "https://attsgwsxdlblbqxnboqx.supabase.co/rest/v1/exercises?name=eq.${name}&select=*"
    val request = Request.Builder()
        .url(url)
        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        .build()
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string() ?: return@withContext null
    val jsonArray = JSONArray(responseBody)
    if (jsonArray.length() == 0) return@withContext null
    val obj = jsonArray.getJSONObject(0)
    Exercise(
        name = obj.getString("name"),
        force = obj.optString("force", ""),
        level = obj.optString("level", ""),
        mechanic = obj.optString("mechanic", ""),
        equipment = obj.optString("equipment", null),
        primaryMuscles = obj.optJSONArray("primary_muscles")?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList(),
        secondaryMuscles = obj.optJSONArray("secondary_muscles")?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList(),
        instructions = obj.optJSONArray("instructions")?.let { arr -> List(arr.length()) { arr.getString(it) } } ?: emptyList(),
        category = obj.optString("category", "")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseName: String
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(2) }

    LaunchedEffect(exerciseName) {
        isLoading = true
        error = null
        try {
            exercise = fetchExerciseByName(exerciseName)
        } catch (e: Exception) {
            error = "Could not load exercise."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exerciseName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                exercise == null -> Text("Oefening niet gevonden")
                else -> {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = exercise!!.name,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Level: ", fontWeight = FontWeight.SemiBold)
                                Text(exercise!!.level)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Kracht: ", fontWeight = FontWeight.SemiBold)
                                Text(exercise!!.force)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Mechaniek: ", fontWeight = FontWeight.SemiBold)
                                Text(exercise!!.mechanic)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Apparatuur: ", fontWeight = FontWeight.SemiBold)
                                Text(exercise!!.equipment ?: "No equipment needed")
                            }
                            Spacer(Modifier.height(8.dp))
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text("Primaire Spieren:", fontWeight = FontWeight.SemiBold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                exercise!!.primaryMuscles.forEach {
                                    AssistChip(onClick = {}, label = { Text(it) })
                                }
                            }
                            if (exercise!!.secondaryMuscles.isNotEmpty()) {
                                Text("Secundaire Spieren:", fontWeight = FontWeight.SemiBold)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    exercise!!.secondaryMuscles.forEach {
                                        AssistChip(onClick = {}, label = { Text(it) })
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Categorie: ${exercise!!.category}", fontWeight = FontWeight.SemiBold)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text("Instructies:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            exercise!!.instructions.forEachIndexed { idx, instr ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text("${idx + 1}.", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(instr)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}