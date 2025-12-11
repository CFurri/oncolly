package com.teknos.oncolly.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teknos.oncolly.screens.DoctorScreen
import com.teknos.oncolly.screens.LoginScreen
import com.teknos.oncolly.screens.PacientScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { tipusUsuari ->
                    if (tipusUsuari == "doctor") {
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
            DoctorScreen(onLogout = {
                navController.navigate("login") { popUpTo(0) }
            })
        }

        composable("home_pacient") {
            PacientScreen(onLogout = {
                navController.navigate("login") { popUpTo(0) }
            })
        }
    }
}