package com.example.workoutbuddyapplication.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

data class StringResources(
    // Settings Screen
    val settings: String,
    val language: String,
    val chooseLanguage: String,
    val close: String,
    val editProfile: String,
    val editProfileSubtitle: String,
    val notifications: String,
    val notificationsSubtitle: String,
    val units: String,
    val metric: String,
    val imperial: String,
    val darkMode: String,
    val darkModeEnabled: String,
    val lightModeEnabled: String,
    val logout: String,
    val logoutConfirm: String,
    val cancel: String,
    val logoutSubtitle: String,
    
    // Common
    val back: String,
    val save: String,
    val delete: String,
    val add: String,
    val search: String,
    val yes: String,
    val no: String,
    val ok: String,
    
    // Login/Signup
    val welcomeBack: String,
    val appName: String,
    val appTagline: String,
    val login: String,
    val createAccount: String,
    val email: String,
    val password: String,
    val name: String,
    
    // Dashboard
    val welcomeToAktiv: String,
    val recentWorkouts: String,
    val startWorkout: String,
    val workouts: String,
    val distance: String,
    val time: String,
    val addManually: String,
    val noWorkoutsFound: String,
    val minutes: String,
    
    // Profile
    val profile: String,
    val changePassword: String,
    
    // Workout Types
    val strengthTraining: String,
    val running: String,
    val yoga: String,
    val chooseWorkoutType: String,
    
    // Strength Training
    val addPreset: String,
    val addExercises: String,
    val stopWorkout: String,
    val set: String,
    val previous: String,
    val reps: String,
    val restTime: String,
    val exercises: String,
    val workoutPresets: String,
    val setRestTime: String,
    val updateRestTime: String,
    val restTimeFormat: String,
    val deleteExercise: String,
    val deleteExerciseConfirm: String,
    val addSet: String,
    
    // Running
    val setGoal: String,
    val useHeartRateZones: String,
    val goalDistance: String,
    val goalTime: String,
    val progressToGoal: String,
    val heartRateZone: String,
    
    // Yoga
    val currentPose: String,
    val difficulty: String,
    val benefits: String,
    val breathing: String,
    val nextPoses: String,
    val chooseYogaRoutine: String,
    val newYogaRoutine: String,
    val addNewPose: String,
    
    // Workout Completion
    val workoutCompleted: String,
    val wellDone: String,
    val workoutSummary: String,
    val duration: String,
    val calories: String,
    
    // History & Stats
    val workoutHistory: String,
    val statistics: String,
    val activeGoals: String,
    val viewGoals: String,
    
    // Bluetooth & Tracking
    val trackingOptions: String,
    val bluetoothDevices: String,
    val connectSmartwatch: String,
    val searchingDevices: String,
    val availableDevices: String,
    
    // Bottom Navigation
    val dashboard: String,
    val history: String,
    val stats: String,
    
    // Other
    val seconds: String,
    val workoutType: String,
    val debugMode: String
)

val LocalStringResources = staticCompositionLocalOf<StringResources> {
    error("No StringResources provided")
}

val dutchStrings = StringResources(
    // Settings Screen
    settings = "Instellingen",
    language = "Taal",
    chooseLanguage = "Taal kiezen",
    close = "Sluiten",
    editProfile = "Profiel bewerken",
    editProfileSubtitle = "Pas je profielgegevens aan",
    notifications = "Notificaties",
    notificationsSubtitle = "Beheer je notificatie-instellingen",
    units = "Eenheden",
    metric = "Metrisch (kg, km)",
    imperial = "Imperial (lbs, mi)",
    darkMode = "Donkere modus",
    darkModeEnabled = "Donker thema actief",
    lightModeEnabled = "Licht thema actief",
    logout = "Uitloggen",
    logoutConfirm = "Weet je zeker dat je wilt uitloggen?",
    cancel = "Annuleren",
    logoutSubtitle = "Log uit van je account",
    
    // Common
    back = "Terug",
    save = "Opslaan",
    delete = "Verwijderen",
    add = "Toevoegen",
    search = "Zoeken",
    yes = "Ja",
    no = "Nee",
    ok = "OK",
    
    // Login/Signup
    welcomeBack = "Welkom terug",
    appName = "Aktiv",
    appTagline = "Jouw persoonlijke trainingsmaatje",
    login = "Inloggen",
    createAccount = "Account Aanmaken",
    email = "Email",
    password = "Wachtwoord",
    name = "Naam",
    
    // Dashboard
    welcomeToAktiv = "Welkom bij Aktiv",
    recentWorkouts = "Recente Workouts",
    startWorkout = "Workout starten",
    workouts = "Workouts",
    distance = "Afstand",
    time = "Tijd",
    addManually = "Handmatig toevoegen",
    noWorkoutsFound = "Geen workouts gevonden",
    minutes = "minuten",
    
    // Profile
    profile = "Profiel",
    changePassword = "Verander Wachtwoord",
    
    // Workout Types
    strengthTraining = "Krachttraining",
    running = "Hardlopen",
    yoga = "Yoga",
    chooseWorkoutType = "Kies je workout type",
    
    // Strength Training
    addPreset = "Preset toevoegen",
    addExercises = "Oefeningen toevoegen",
    stopWorkout = "Stop workout",
    set = "Set",
    previous = "Vorige",
    reps = "reps",
    restTime = "Rust tijd",
    exercises = "Oefeningen",
    workoutPresets = "Workout Presets",
    setRestTime = "Rust tijd instellen",
    updateRestTime = "Update rust tijd",
    restTimeFormat = "RUST TIJD: %s",
    deleteExercise = "Oefening verwijderen",
    deleteExerciseConfirm = "Weet je zeker dat je '%s' wilt verwijderen?",
    addSet = "Set toevoegen",
    
    // Running
    setGoal = "Stel je doel in",
    useHeartRateZones = "Hartslagzones gebruiken",
    goalDistance = "Doel afstand",
    goalTime = "Doel: %s in %d min",
    progressToGoal = "Voortgang naar doel",
    heartRateZone = "Hartslagzone",
    
    // Yoga
    currentPose = "Huidige Pose",
    difficulty = "Moeilijkheid: %s",
    benefits = "Voordelen: %s",
    breathing = "Ademhaling: %s",
    nextPoses = "Volgende Poses",
    chooseYogaRoutine = "Kies een Yoga Routine",
    newYogaRoutine = "Nieuwe Yoga Routine",
    addNewPose = "Nieuwe Pose Toevoegen",
    
    // Workout Completion
    workoutCompleted = "Workout Voltooid!",
    wellDone = "Goed gedaan! Je hebt je workout succesvol afgerond.",
    workoutSummary = "Workout Samenvatting",
    duration = "Duur",
    calories = "Calorieën",
    
    // History & Stats
    workoutHistory = "Workout Geschiedenis",
    statistics = "Statistieken",
    activeGoals = "Actieve Doelen",
    viewGoals = "Doelen Bekijken",
    
    // Bluetooth & Tracking
    trackingOptions = "Tracking Opties",
    bluetoothDevices = "Bluetooth-apparaten",
    connectSmartwatch = "Verbind met smartwatch of sporthorloge",
    searchingDevices = "Zoeken naar apparaten...",
    availableDevices = "Beschikbare Apparaten",
    
    // Bottom Navigation
    dashboard = "Dashboard",
    history = "Geschiedenis",
    stats = "Statistieken",
    
    // Other
    seconds = "seconden",
    workoutType = "Workout Type",
    debugMode = "DEBUG MODE"
)

val englishStrings = StringResources(
    // Settings Screen
    settings = "Settings",
    language = "Language",
    chooseLanguage = "Choose Language",
    close = "Close",
    editProfile = "Edit Profile",
    editProfileSubtitle = "Update your profile information",
    notifications = "Notifications",
    notificationsSubtitle = "Manage your notification settings",
    units = "Units",
    metric = "Metric (kg, km)",
    imperial = "Imperial (lbs, mi)",
    darkMode = "Dark Mode",
    darkModeEnabled = "Dark theme active",
    lightModeEnabled = "Light theme active",
    logout = "Logout",
    logoutConfirm = "Are you sure you want to logout?",
    cancel = "Cancel",
    logoutSubtitle = "Sign out of your account",
    
    // Common
    back = "Back",
    save = "Save",
    delete = "Delete",
    add = "Add",
    search = "Search",
    yes = "Yes",
    no = "No",
    ok = "OK",
    
    // Login/Signup
    welcomeBack = "Welcome back",
    appName = "Aktiv",
    appTagline = "Your personal training buddy",
    login = "Login",
    createAccount = "Create Account",
    email = "Email",
    password = "Password",
    name = "Name",
    
    // Dashboard
    welcomeToAktiv = "Welcome to Aktiv",
    recentWorkouts = "Recent Workouts",
    startWorkout = "Start Workout",
    workouts = "Workouts",
    distance = "Distance",
    time = "Time",
    addManually = "Add Manually",
    noWorkoutsFound = "No workouts found",
    minutes = "minutes",
    
    // Profile
    profile = "Profile",
    changePassword = "Change Password",
    
    // Workout Types
    strengthTraining = "Strength Training",
    running = "Running",
    yoga = "Yoga",
    chooseWorkoutType = "Choose your workout type",
    
    // Strength Training
    addPreset = "Add Preset",
    addExercises = "Add Exercises",
    stopWorkout = "Stop Workout",
    set = "Set",
    previous = "Previous",
    reps = "reps",
    restTime = "Rest Time",
    exercises = "Exercises",
    workoutPresets = "Workout Presets",
    setRestTime = "Set Rest Time",
    updateRestTime = "Update Rest Time",
    restTimeFormat = "REST TIME: %s",
    deleteExercise = "Delete Exercise",
    deleteExerciseConfirm = "Are you sure you want to delete '%s'?",
    addSet = "Add Set",
    
    // Running
    setGoal = "Set Your Goal",
    useHeartRateZones = "Use Heart Rate Zones",
    goalDistance = "Goal Distance",
    goalTime = "Goal: %s in %d min",
    progressToGoal = "Progress to Goal",
    heartRateZone = "Heart Rate Zone",
    
    // Yoga
    currentPose = "Current Pose",
    difficulty = "Difficulty: %s",
    benefits = "Benefits: %s",
    breathing = "Breathing: %s",
    nextPoses = "Next Poses",
    chooseYogaRoutine = "Choose a Yoga Routine",
    newYogaRoutine = "New Yoga Routine",
    addNewPose = "Add New Pose",
    
    // Workout Completion
    workoutCompleted = "Workout Completed!",
    wellDone = "Well done! You have successfully completed your workout.",
    workoutSummary = "Workout Summary",
    duration = "Duration",
    calories = "Calories",
    
    // History & Stats
    workoutHistory = "Workout History",
    statistics = "Statistics",
    activeGoals = "Active Goals",
    viewGoals = "View Goals",
    
    // Bluetooth & Tracking
    trackingOptions = "Tracking Options",
    bluetoothDevices = "Bluetooth Devices",
    connectSmartwatch = "Connect to smartwatch or sports watch",
    searchingDevices = "Searching for devices...",
    availableDevices = "Available Devices",
    
    // Bottom Navigation
    dashboard = "Dashboard",
    history = "History",
    stats = "Stats",
    
    // Other
    seconds = "seconds",
    workoutType = "Workout Type",
    debugMode = "DEBUG MODE"
)

@Composable
@ReadOnlyComposable
fun strings(): StringResources = LocalStringResources.current 