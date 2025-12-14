package com.teknos.oncolly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.teknos.oncolly.screens.patient.BottomNavigationBar
import com.teknos.oncolly.screens.patient.DynamicActivityScreen
import com.teknos.oncolly.screens.patient.PacientScreen
import com.teknos.oncolly.screens.splash.SplashScreen
import com.teknos.oncolly.viewmodel.ActivitiesViewModel
import com.teknos.oncolly.viewmodel.PatientViewModel
import com.teknos.oncolly.screens.patient.PatientActivitiesScreen
import com.teknos.oncolly.screens.patient.ProfileScreen


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
                onLogout = { navController.navigate("login") { popUpTo(0) } },
                onActivityClick = { activityId ->
                    navController.navigate("activity_screen/$activityId")
                },
                // AQUI ESTÀ L'ONCLICK QUE DEMANAVES:
                onNavigateToActivitiesList = {
                    navController.navigate("patient_activities_list")
                },
                onNavigateToProfile = { navController.navigate("profile_pacient") } // <--- AFEGEIX AIXÒ
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

        composable("patient_activities_list") {
            // 1. Instanciem el ViewModel
            // Si aquí et surt vermell, assegura't de tenir l'import: androidx.lifecycle.viewmodel.compose.viewModel
            val viewModel: ActivitiesViewModel = viewModel()

            // 2. Estructura amb Scaffold
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        currentTab = 1,
                        onNavigateToHome = { navController.navigate("home_pacient") { popUpTo("home_pacient") { inclusive = true } } },
                        onNavigateToActivities = {},
                        onNavigateToProfile = { navController.navigate("profile_pacient") }
                    )
                }
            ) { paddingValues ->
                // 3. Contingut
                Box(modifier = Modifier.padding(paddingValues)) {
                    PatientActivitiesScreen(viewModel = viewModel)
                }
            }
        }

        composable("profile_pacient") {
            ProfileScreen(
                onLogout = {
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