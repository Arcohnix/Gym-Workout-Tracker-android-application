package com.example.forgegym.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Goals : Screen("goals")
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object ExerciseLibrary : Screen("exercise_library")
    data object ExerciseDetail : Screen("exercise/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise/$exerciseId"
    }
    data object WorkoutDetail : Screen("workout/{workoutId}") {
        fun createRoute(workoutId: String) = "workout/$workoutId"
    }
    data object WorkoutBuilder : Screen("builder?workoutId={workoutId}") {
        fun createRoute(workoutId: String? = null) = if (workoutId != null) "builder?workoutId=$workoutId" else "builder"
    }
    data object Session : Screen("session/{sessionId}") {
        fun createRoute(sessionId: String) = "session/$sessionId"
    }
    data object Summary : Screen("summary/{sessionId}") {
        fun createRoute(sessionId: String) = "summary/$sessionId"
    }
    data object History : Screen("history")
    data object Progress : Screen("progress")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Privacy : Screen("privacy")
    data object About : Screen("about")
}
