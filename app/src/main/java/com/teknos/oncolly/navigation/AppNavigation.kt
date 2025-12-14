package com.teknos.oncolly.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teknos.oncolly.screens.auth.LoginScreen
import com.teknos.oncolly.screens.doctor.DoctorScreen
import com.teknos.oncolly.screens.doctor.PacientDetailScreen
import com.teknos.oncolly.screens.patient.ActivityType
import com.teknos.oncolly.screens.patient.DynamicActivityScreen
import com.teknos.oncolly.screens.patient.PacientScreen
import com.teknos.oncolly.screens.patient.PatientActivitiesScreen
import com.teknos.oncolly.screens.patient.ProfileScreen
import com.teknos.oncolly.screens.splash.SplashScreen
import com.teknos.oncolly.singletons.SingletonApp
import com.teknos.oncolly.utils.SessionManager
import com.teknos.oncolly.viewmodel.ActivitiesViewModel
import com.teknos.oncolly.viewmodel.PatientViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {

        composable("splash") {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { tipusUsuari ->
                    if (tipusUsuari.equals("DOCTOR", ignoreCase = true)) {
                        navController.navigate("home_doctor") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home_pacient") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("home_doctor") {
            DoctorScreen(
                onLogout = {
                    SessionManager.clearSession(context)
                    SingletonApp.getInstance().tancarSessio()
                    navController.navigate("login") { popUpTo(0) }
                },
                onPacientClick = { idPacient ->
                    navController.navigate("detail_pacient/$idPacient")
                }
            )
        }

        composable(
            route = "detail_pacient/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            PacientDetailScreen(
                pacientId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable("home_pacient") {
            PacientScreen(
                onLogout = {
                    SessionManager.clearSession(context)
                    SingletonApp.getInstance().tancarSessio()
                    navController.navigate("login") { popUpTo(0) }
                },
                onActivityClick = { activityId ->
                    navController.navigate("activity_screen/$activityId")
                },
                onNavigateToActivitiesList = {
                    navController.navigate("patient_activities_list")
                },
                onNavigateToProfile = { navController.navigate("profile_pacient") }
            )
        }

        composable(route = "activity_screen/{typeId}") { backStackEntry ->
            val typeId = backStackEntry.arguments?.getString("typeId")
            val type = ActivityType.values().find { it.id == typeId }

            if (type != null) {
                val patientViewModel: PatientViewModel = viewModel()
                DynamicActivityScreen(
                    activityType = type,
                    viewModel = patientViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("patient_activities_list") {
            val viewModel: ActivitiesViewModel = viewModel()
            PatientActivitiesScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("home_pacient") {
                        popUpTo("home_pacient") { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile_pacient")
                }
            )
        }

        composable("profile_pacient") {
            ProfileScreen(
                onLogout = {
                    SessionManager.clearSession(context)
                    SingletonApp.getInstance().tancarSessio()
                    navController.navigate("login") { popUpTo(0) }
                },
                onNavigateToHome = {
                    navController.navigate("home_pacient") {
                        popUpTo("home_pacient") { inclusive = true }
                    }
                },
                onNavigateToActivities = {
                    navController.navigate("patient_activities_list")
                }
            )
        }
    }
}