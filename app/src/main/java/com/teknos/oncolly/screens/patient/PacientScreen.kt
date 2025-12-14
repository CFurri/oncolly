package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.singletons.SingletonApp

// --- COLORS EXTRETS DEL DISSENY (CSS) ---
val PrimaryBlue = Color(0xFF259DF4) // El blau del CSS
val SecondaryGreen = Color(0xFF66BB6A) // Un verd similar al de la imatge
val TextGrey = Color(0xFF565D6D)    // neutral-600 del CSS

@Composable
fun PacientScreen(
    onLogout: () -> Unit,
    onActivityClick: (String) -> Unit,
    onNavigateToActivitiesList: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        // 1. BARRA DE NAVEGACIÓ INFERIOR (Amb la línia groga)
        bottomBar = {
            BottomNavigationBar(
                currentTab = 0,
                onNavigateToHome = { /* Ja hi som, no cal fer res o recarregar */ },
                onNavigateToActivities = onNavigateToActivitiesList,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color(0xFFF8F9FA) // Fons gris molt claret
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 2. CAPÇALERA
            HeaderPacient()

            // 3. EL "SISTEMA SOLAR" DE BOTONS
            // Utilitzem un Box per poder posar elements on vulguem
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // Ocupa tot l'espai disponible
                contentAlignment = Alignment.Center // Tot parteix del centre
            ) {
                // --- DEFINIM ELS BOTONS I LA SEVA POSICIÓ (X, Y) ---

                // Centre - Dalt: Walking
                ActivityBubble(
                    text = "Walking",
                    icon = Icons.Default.DirectionsWalk,
                    color = PrimaryBlue,
                    offsetX = 0.dp, offsetY = (-120).dp,
                    onClick = { onActivityClick("walking") }
                )

                // Esquerra: Exercise
                ActivityBubble(
                    text = "Exercise",
                    icon = Icons.Default.FitnessCenter,
                    color = PrimaryBlue,
                    offsetX = (-100).dp, offsetY = (-20).dp,
                    onClick = { onActivityClick("exercise") }
                )

                // Dreta: Eating
                ActivityBubble(
                    text = "Eating",
                    icon = Icons.Default.Restaurant, // O RiceBowl
                    color = SecondaryGreen,
                    offsetX = 100.dp, offsetY = (-20).dp,
                    onClick = { onActivityClick("eating") }
                )

                // A sota Esquerra (Blanc): Deposicions
                ActivityBubble(
                    text = "Depositions",
                    icon = Icons.Default.MusicNote,
                    color = Color.White,
                    textColor = Color.Black,
                    offsetX = (-110).dp, offsetY = 100.dp,
                    onClick = { onActivityClick("depositions") }
                )

                // A sota Dreta (Blanc): Medication
                ActivityBubble(
                    text = "Medication",
                    icon = Icons.Default.Medication,
                    color = Color.White,
                    textColor = Color.Black,
                    offsetX = 110.dp, offsetY = 100.dp,
                    onClick = { onActivityClick("medication") }
                )

                // A baix Centre-Esquerra: Sleep
                ActivityBubble(
                    text = "Sleep",
                    icon = Icons.Default.Bed,
                    color = SecondaryGreen,
                    offsetX = (-40).dp, offsetY = 200.dp,
                    onClick = { onActivityClick("sleep") }
                )

                // A baix Centre-Dreta: Hydration
                ActivityBubble(
                    text = "Hydration",
                    icon = Icons.Default.LocalDrink,
                    color = PrimaryBlue,
                    offsetX = 60.dp, offsetY = 210.dp,
                    onClick = { onActivityClick("hydration") }
                )
            }
        }
    }
}

// --- COMPONENTS REUTILITZABLES ---

@Composable
fun HeaderPacient() {
    val pacient = SingletonApp.getInstance().pacientActual
    val nomAMostrar = pacient?.email ?: "Pacient"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icona del cor al quadrat (Logo)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PrimaryBlue,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MonitorHeart,
                    contentDescription = "Logo",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column{
            Text(
                text = "Hola, $nomAMostrar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextGrey
            )
            Text(
                text = "Patient Home",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextGrey
            )
        }
    }
}


// --- VERSIÓ CORRECTA SEGONS FOTO (TEXT DINS) ---
@Composable
fun ActivityBubble(
    text: String,
    icon: ImageVector,
    color: Color,
    textColor: Color = Color.White, // Per defecte blanc
    offsetX: Dp,
    offsetY: Dp,
    size: Dp = 90.dp, // Una mica més petit per no xocar
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = Color(0xFF000000).copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        shape = CircleShape,
        color = color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (color == Color.White) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (color == Color.White) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: Int,
    onNavigateToHome: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        // Línia groga (simulada amb un Box a dalt)
        Box {
            // La línia groga
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFFFDD835)) // Groc
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomNavItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    isSelected = currentTab == 0,
                    onClick = onNavigateToHome
                )
                BottomNavItem(
                    icon = Icons.Outlined.Assignment,
                    label = "Activities",
                    isSelected = currentTab == 1,
                    onClick = onNavigateToActivities
                )
                BottomNavItem(
                    icon = Icons.Outlined.Person,
                    label = "Profile",
                    isSelected = currentTab == 2,
                    onClick = onNavigateToProfile
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) PrimaryBlue else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color)
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}