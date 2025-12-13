package com.teknos.oncolly.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teknos.oncolly.screens.DoctorScreen
import com.teknos.oncolly.screens.LoginScreen
import com.teknos.oncolly.screens.PacientDetailScreen // <--- Assegura't de tenir aquest import
import com.teknos.oncolly.screens.PacientScreen
import com.teknos.oncolly.screens.SplashScreen

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

        // --- AQUESTA ÉS LA PART QUE T'HAVIES DEIXAT ---
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
        // ---------------------------------------------

        // AQUESTA ÉS LA QUE HAS ARREGLAT ARA
        composable("home_pacient") {
            PacientScreen(
                onLogout = {
                    navController.navigate("login") { popUpTo(0) }
                }
            )
        }
    }
}