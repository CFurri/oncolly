package com.teknos.oncolly.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
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
        startDestination = "splash"
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

        // --- RUTES DE DOCTOR ---
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
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            // Animació simple per entrar al detall (dreta a esquerra)
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            PacientDetailScreen(
                pacientId = id,
                onBack = { navController.popBackStack() }
            )
        }

        // --- RUTES DE PACIENT (AMB ANIMACIÓ INTEL·LIGENT DE TABS) ---

        // 1. HOME (Index 0)
        composable(
            route = "home_pacient",
            enterTransition = { tabEnterTransition(this) },
            exitTransition = { tabExitTransition(this) }
        ) {
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
                    navController.navigate("patient_activities_list") {
                        // Això és important per a que l'animació es vegi bé i no es "recreï" la pantalla
                        popUpTo("home_pacient") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile_pacient") {
                        popUpTo("home_pacient") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // 2. ACTIVITIES LIST (Index 1)
        composable(
            route = "patient_activities_list",
            enterTransition = { tabEnterTransition(this) },
            exitTransition = { tabExitTransition(this) }
        ) {
            val viewModel: ActivitiesViewModel = viewModel()
            PatientActivitiesScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("home_pacient") {
                        popUpTo("home_pacient") { inclusive = false } // No matem la home
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile_pacient") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // 3. PROFILE (Index 2)
        composable(
            route = "profile_pacient",
            enterTransition = { tabEnterTransition(this) },
            exitTransition = { tabExitTransition(this) }
        ) {
            ProfileScreen(
                onLogout = {
                    SessionManager.clearSession(context)
                    SingletonApp.getInstance().tancarSessio()
                    navController.navigate("login") { popUpTo(0) }
                },
                onNavigateToHome = {
                    navController.navigate("home_pacient") {
                        popUpTo("home_pacient") { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToActivities = {
                    navController.navigate("patient_activities_list") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Pantalla de detall d'activitat (fora de les tabs)
        composable(
            route = "activity_screen/{typeId}",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
        ) { backStackEntry ->
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
    }
}

// --- FUNCIONS PER CALCULAR LA DIRECCIÓ ---

// Assignem un número a cada pantalla per saber l'ordre
private fun getRouteIndex(route: String?): Int {
    return when (route) {
        "home_pacient" -> 0
        "patient_activities_list" -> 1
        "profile_pacient" -> 2
        else -> -1 // Si no és una tab (ex: login), no entra al joc
    }
}

// Calcula si entra per la dreta o per l'esquerra
private fun tabEnterTransition(scope: AnimatedContentTransitionScope<NavBackStackEntry>) = with(scope) {
    val fromIndex = getRouteIndex(initialState.destination.route)
    val toIndex = getRouteIndex(targetState.destination.route)

    if (fromIndex != -1 && toIndex != -1) {
        if (toIndex > fromIndex) {
            // Anem endavant (0->1, 1->2): Entra per la DRETA
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
        } else {
            // Anem enrere (1->0, 2->1): Entra per l'ESQUERRA
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
        }
    } else {
        // Per defecte (si venim del Login, etc)
        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
    }
}

// Calcula cap on surt la pantalla vella
private fun tabExitTransition(scope: AnimatedContentTransitionScope<NavBackStackEntry>) = with(scope) {
    val fromIndex = getRouteIndex(initialState.destination.route)
    val toIndex = getRouteIndex(targetState.destination.route)

    if (fromIndex != -1 && toIndex != -1) {
        if (toIndex > fromIndex) {
            // Anem endavant: La vella surt cap a l'ESQUERRA
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
        } else {
            // Anem enrere: La vella surt cap a la DRETA
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
        }
    } else {
        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
    }
}