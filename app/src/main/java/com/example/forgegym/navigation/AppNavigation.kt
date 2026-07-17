package com.example.forgegym.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.forgegym.ui.screens.history.HistoryScreen
import com.example.forgegym.ui.screens.library.ExerciseDetailScreen
import com.example.forgegym.ui.screens.library.ExerciseLibraryScreen
import com.example.forgegym.ui.screens.home.HomeScreen
import com.example.forgegym.ui.screens.library.WorkoutLibraryScreen
import com.example.forgegym.ui.screens.onboarding.GoalScreen
import com.example.forgegym.ui.screens.onboarding.WelcomeScreen
import com.example.forgegym.ui.screens.profile.ProfileScreen
import com.example.forgegym.ui.screens.progress.ProgressScreen
import com.example.forgegym.ui.screens.session.WorkoutSessionScreen
import com.example.forgegym.ui.screens.settings.AboutScreen
import com.example.forgegym.ui.screens.settings.PrivacyScreen
import com.example.forgegym.ui.screens.settings.SettingsScreen
import com.example.forgegym.ui.screens.splash.SplashScreen
import com.example.forgegym.ui.screens.workout.WorkoutBuilderScreen
import com.example.forgegym.ui.screens.workout.WorkoutDetailScreen
import com.example.forgegym.ui.screens.workout.WorkoutSummaryScreen
import com.example.forgegym.viewmodel.OnboardingViewModel

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavHostController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    isOnboardingCompleted: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isOnboardingCompleted) Screen.Home.route else "onboarding",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(Screen.Splash.route) { SplashScreen() }
        
        navigation(
            startDestination = Screen.Welcome.route,
            route = "onboarding"
        ) {
            composable(Screen.Welcome.route) { entry ->
                val viewModel = entry.sharedViewModel<OnboardingViewModel>(navController)
                WelcomeScreen(
                    onNextClick = { navController.navigate(Screen.Goals.route) },
                    viewModel = viewModel
                )
            }
            composable(Screen.Goals.route) { entry ->
                val viewModel = entry.sharedViewModel<OnboardingViewModel>(navController)
                GoalScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onLibraryClick = { navController.navigate(Screen.ExerciseLibrary.route) },
                onProgressClick = { navController.navigate(Screen.Progress.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                },
                onContinueClick = { sessionId ->
                    navController.navigate(Screen.Session.createRoute(sessionId))
                },
                onAnalyticsClick = { navController.navigate(Screen.Progress.route) },
                onWorkoutBuilderClick = { navController.navigate(Screen.WorkoutBuilder.createRoute()) }
            )
        }

        composable(Screen.Library.route) {
            WorkoutLibraryScreen(
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                },
                onCreateWorkoutClick = {
                    navController.navigate(Screen.WorkoutBuilder.createRoute())
                }
            )
        }

        composable(Screen.ExerciseLibrary.route) {
            ExerciseLibraryScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) {
            ExerciseDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) {
            WorkoutDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.WorkoutBuilder.createRoute(id))
                },
                onStartWorkout = { id ->
                    navController.navigate(Screen.Session.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.WorkoutBuilder.route,
            arguments = listOf(navArgument("workoutId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            WorkoutBuilderScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                onViewExerciseDetails = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                }
            )
        }

        composable(
            route = Screen.Session.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            WorkoutSessionScreen(
                onFinishSession = { id ->
                    navController.navigate(Screen.Summary.createRoute(id)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onDiscardSession = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Summary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            WorkoutSummaryScreen(
                sessionId = sessionId,
                onFinish = {
                    navController.popBackStack(Screen.Home.route, false)
                },
                onRepeat = { sessionId ->
                    navController.navigate(Screen.Session.createRoute(sessionId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.Summary.createRoute(sessionId))
                }
            )
        }
        composable(Screen.Progress.route) { ProgressScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onPrivacyClick = { navController.navigate(Screen.Privacy.route) },
                onAboutClick = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
