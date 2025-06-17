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
    val edit: String,
    val select: String,
    
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
    
    // Workout Details
    val workoutDetails: String,
    val date: String,
    val notes: String,
    val workoutNotFound: String,
    val failedToLoadWorkoutDetails: String,
    val sets: String,
    val weight: String,
    
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
    
    // Cycling
    val cycling: String,
    
    // Workout Completion
    val workoutCompleted: String,
    val wellDone: String,
    val workoutSummary: String,
    val duration: String,
    val calories: String,
    val congratulations: String,
    val steps: String,
    val backToDashboard: String,
    val share: String,
    val sharePerformance: String,
    
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
    val debugMode: String,
    
    // StartWorkout Screen
    val startWorkoutTitle: String,
    val pairDevice: String,
    
    // StrengthWorkout Screen
    val strengthWorkoutTitle: String,
    val setRestTimeFor: String,
    val menu: String,
    val completed: String,
    val searchExercises: String,
    val exercisesCount: String,
    val setRestTimeInMinutes: String,
    val exercisesCountFormat: String,

    // Add Workout Screen
    val addWorkoutTitle: String,
    val typeName: String,
    val selectDate: String,
    val durationMinutes: String,
    val distanceKm: String,
    val saveError: String,
    val noUserLoggedIn: String,
    val saving: String,
    val saveWorkout: String,

    // Goals Screen
    val editGoal: String,
    val title: String,
    val selectWorkoutType: String,
    val selectGoalType: String,
    val goalType: String,
    val value: String,
    val unit: String,
    val startDate: String,
    val endDate: String,
    val updateError: String,
    val updateFailed: String,
    val createFailed: String,
    val goalUpdatedSuccess: String,
    val update: String,
    val newGoal: String,
    val goalCreatedSuccess: String,
    val create: String,
    val deleteGoal: String,
    val deleteGoalConfirm: String,
    val goals: String,
    val loading: String,
    val loadingError: String,
    val noGoalsFound: String,
    val addGoalPrompt: String,
    val of: String,
    val completedGoal: String,

    // Strength Presets
    val pushWorkout: String,
    val pullWorkout: String,
    val legWorkout: String,
    val fullBodyWorkout: String,

    // Bluetooth Screen
    val bluetoothRequired: String,
    val bluetoothDisabled: String,
    val bluetoothDisabledPrompt: String,
    val enable: String,
    val permissionsRequired: String,
    val permissionsRequiredPrompt: String,
    val connect: String,
    val connected: String,
    val disconnect: String,
    val refresh: String,
    val noDevicesFound: String,
    val ensureDeviceIsNear: String
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
    edit = "Bewerken",
    select = "Selecteren",
    
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
    
    // Workout Details
    workoutDetails = "Workout Details",
    date = "Datum",
    notes = "Notities",
    workoutNotFound = "Workout Niet Gevonden",
    failedToLoadWorkoutDetails = "Fout bij het laden van workout details",
    sets = "Sets",
    weight = "Gewicht",
    
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

    // Cycling
    cycling = "Fietsen",
    
    // Workout Completion
    workoutCompleted = "Workout Voltooid!",
    wellDone = "Goed gedaan! Je hebt je workout succesvol afgerond.",
    workoutSummary = "Workout Samenvatting",
    duration = "Duur",
    calories = "Calorieën",
    congratulations = "Gefeliciteerd!",
    steps = "Stappen",
    backToDashboard = "Terug naar Dashboard",
    share = "Delen",
    sharePerformance = "Deel prestaties",
    
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
    debugMode = "DEBUG MODE",
    
    // StartWorkout Screen
    startWorkoutTitle = "Workout Starten",
    pairDevice = "Apparaat Pairen",
    
    // StrengthWorkout Screen
    strengthWorkoutTitle = "Krachttraining",
    setRestTimeFor = "Rust tijd instellen voor",
    menu = "Menu",
    completed = "Voltooid",
    searchExercises = "Oefeningen zoeken",
    exercisesCount = "Aantal oefeningen",
    setRestTimeInMinutes = "Rust tijd instellen in minuten",
    exercisesCountFormat = "(%d oefeningen)",

    // Add Workout Screen
    addWorkoutTitle = "Workout Toevoegen",
    typeName = "Type Workout",
    selectDate = "Selecteer Datum",
    durationMinutes = "Duur in Minuten",
    distanceKm = "Afstand in Km",
    saveError = "Fout bij Opslaan",
    noUserLoggedIn = "Geen Gebruiker Ingelogd",
    saving = "Opslaan...",
    saveWorkout = "Workout Opslaan",

    // Goals Screen
    editGoal = "Doel Bewerken",
    title = "Titel",
    selectWorkoutType = "Workout Type Selecteren",
    selectGoalType = "Doel Type Selecteren",
    goalType = "Doel Type",
    value = "Waarde",
    unit = "Eenheid",
    startDate = "Start Datum",
    endDate = "Eind Datum",
    updateError = "Update Fout",
    updateFailed = "Update Mislukt",
    createFailed = "Aanmaken Mislukt",
    goalUpdatedSuccess = "Doel Succesvol Bijgewerkt",
    update = "Bijwerken",
    newGoal = "Nieuw Doel",
    goalCreatedSuccess = "Doel Succesvol Aangemaakt",
    create = "Aanmaken",
    deleteGoal = "Doel Verwijderen",
    deleteGoalConfirm = "Weet je zeker dat je '%s' wilt verwijderen?",
    goals = "Doelen",
    loading = "Laden...",
    loadingError = "Laden Fout",
    noGoalsFound = "Geen Doelen Gevonden",
    addGoalPrompt = "Voeg Nieuw Doel Toe",
    of = "Van",
    completedGoal = "Voltooid Doel",

    // Strength Presets
    pushWorkout = "Push Workout",
    pullWorkout = "Pull Workout",
    legWorkout = "Leg Workout",
    fullBodyWorkout = "Full Body Workout",

    // Bluetooth Screen
    bluetoothRequired = "Bluetooth vereist",
    bluetoothDisabled = "Bluetooth uitgeschakeld",
    bluetoothDisabledPrompt = "Bluetooth is uitgeschakeld. Wil je het inschakelen?",
    enable = "Inschakelen",
    permissionsRequired = "Toestemming vereist",
    permissionsRequiredPrompt = "Toestemming is vereist om deze functie te gebruiken",
    connect = "Verbinding maken",
    connected = "Verbonden",
    disconnect = "Verbinding verbreken",
    refresh = "Verversen",
    noDevicesFound = "Geen apparaten gevonden",
    ensureDeviceIsNear = "Zorg ervoor dat het apparaat in de buurt is"
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
    edit = "Edit",
    select = "Select",
    
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
    
    // Workout Details
    workoutDetails = "Workout Details",
    date = "Date",
    notes = "Notes",
    workoutNotFound = "Workout Not Found",
    failedToLoadWorkoutDetails = "Failed to load workout details",
    sets = "Sets",
    weight = "Weight",
    
    // Profile
    profile = "Profile",
    changePassword = "Change Password",
    
    // Workout Types
    strengthTraining = "Strength",
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

    // Cycling
    cycling = "Cycling",
    
    // Workout Completion
    workoutCompleted = "Workout Completed!",
    wellDone = "Well done! You have successfully completed your workout.",
    workoutSummary = "Workout Summary",
    duration = "Duration",
    calories = "Calories",
    congratulations = "Congratulations!",
    steps = "Steps",
    backToDashboard = "Back to Dashboard",
    share = "Share",
    sharePerformance = "Share Performance",
    
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
    debugMode = "DEBUG MODE",
    
    // StartWorkout Screen
    startWorkoutTitle = "Start Workout",
    pairDevice = "Pair Device",
    
    // StrengthWorkout Screen
    strengthWorkoutTitle = "Strength Training",
    setRestTimeFor = "Set Rest Time for",
    menu = "Menu",
    completed = "Completed",
    searchExercises = "Search Exercises",
    exercisesCount = "Exercises Count",
    setRestTimeInMinutes = "Set Rest Time in Minutes",
    exercisesCountFormat = "(%d exercises)",

    // Add Workout Screen
    addWorkoutTitle = "Add Workout",
    typeName = "Workout Type",
    selectDate = "Select Date",
    durationMinutes = "Duration in Minutes",
    distanceKm = "Distance in Km",
    saveError = "Save Error",
    noUserLoggedIn = "No User Logged In",
    saving = "Saving...",
    saveWorkout = "Save Workout",

    // Goals Screen
    editGoal = "Edit Goal",
    title = "Title",
    selectWorkoutType = "Select Workout Type",
    selectGoalType = "Select Goal Type",
    goalType = "Goal Type",
    value = "Value",
    unit = "Unit",
    startDate = "Start Date",
    endDate = "End Date",
    updateError = "Update Error",
    updateFailed = "Update Failed",
    createFailed = "Create Failed",
    goalUpdatedSuccess = "Goal Successfully Updated",
    update = "Update",
    newGoal = "New Goal",
    goalCreatedSuccess = "Goal Successfully Created",
    create = "Create",
    deleteGoal = "Delete Goal",
    deleteGoalConfirm = "Are you sure you want to delete '%s'?",
    goals = "Goals",
    loading = "Loading...",
    loadingError = "Loading Error",
    noGoalsFound = "No Goals Found",
    addGoalPrompt = "Add New Goal",
    of = "of",
    completedGoal = "Completed Goal",

    // Strength Presets
    pushWorkout = "Push Workout",
    pullWorkout = "Pull Workout",
    legWorkout = "Leg Workout",
    fullBodyWorkout = "Full Body Workout",

    // Bluetooth Screen
    bluetoothRequired = "Bluetooth required",
    bluetoothDisabled = "Bluetooth disabled",
    bluetoothDisabledPrompt = "Bluetooth is disabled. Would you like to enable it?",
    enable = "Enable",
    permissionsRequired = "Permissions required",
    permissionsRequiredPrompt = "Permissions are required to use this feature",
    connect = "Connect",
    connected = "Connected",
    disconnect = "Disconnect",
    refresh = "Refresh",
    noDevicesFound = "No devices found",
    ensureDeviceIsNear = "Ensure the device is near"
)

@Composable
@ReadOnlyComposable
fun strings(): StringResources = LocalStringResources.current 