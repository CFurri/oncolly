package com.teknos.oncolly.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teknos.oncolly.screens.doctor.DoctorScreen
import com.teknos.oncolly.screens.auth.LoginScreen
import com.teknos.oncolly.screens.doctor.PacientDetailScreen
import com.teknos.oncolly.screens.patient.ActivityType
import com.teknos.oncolly.screens.patient.DynamicActivityScreen
import com.teknos.oncolly.screens.patient.PacientScreen
import com.teknos.oncolly.screens.splash.SplashScreen
import com.teknos.oncolly.viewmodel.PatientViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { tipusUsuari ->
                    if (tipusUsuari == "DOCTOR" || tipusUsuari == "doctor") { // Protecció per majúscules/minúscules
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
                    navController.navigate("login") { popUpTo(0) }
                },
                onPacientClick = { idPacient ->
                    // Naveguem a la pantalla de detall passant l'ID
                    navController.navigate("detail_pacient/$idPacient")
                }
            )
        }

        composable(
            route = "detail_pacient/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }) // Recorda: String, no Int
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
                    navController.navigate("login") { popUpTo(0) }
                },
                onActivityClick = { activityId ->
                    // Aquesta és la màgia: rebem "walking" i naveguem a la pantalla dinàmica
                    navController.navigate("activity_screen/$activityId")
                }
            )
        }

        // --- Per connectar les boles de pacient amb l'activitat ---
        composable(route = "activity_screen/{typeId}") { backStackEntry ->
            val typeId = backStackEntry.arguments?.getString("typeId")
            val type = ActivityType.values().find { it.id == typeId }

            if (type != null) {
                // 1. Inicialitzem el ViewModel
                val patientViewModel: PatientViewModel = viewModel()

                // 2. Cridem la pantalla (Fixa't bé en les comes i el parèntesi final)
                DynamicActivityScreen(
                    activityType = type,
                    viewModel = patientViewModel, // <--- COMA IMPORTANT
                    onBack = { navController.popBackStack() } // <--- DINS del parèntesi
                )
            }
        }
    }
}