package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.singletons.SingletonApp
import kotlin.math.cos
import kotlin.math.sin

// --- ELS TEUS COLORS ORIGINALS ---
val PrimaryBlue = Color(0xFF259DF4)
val SecondaryGreen = Color(0xFF66BB6A)
val TextGrey = Color(0xFF565D6D)

@Composable
fun PacientScreen(
    onLogout: () -> Unit,
    onActivityClick: (String) -> Unit,
    onNavigateToActivitiesList: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // FONS AMB GRADIENT: De blanc (dalt) a blau molt claret (baix)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            PrimaryBlue.copy(alpha = 0.15f) // Un toc suau de color al fons
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentTab = 0,
                onNavigateToHome = { },
                onNavigateToActivities = onNavigateToActivitiesList,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { padding ->

        // Contenidor principal amb el degradat
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. CAPÇALERA
                HeaderPacient()

                // 2. SISTEMA SOLAR (Cercle de botons)
                // BoxWithConstraints ens permet saber l'espai disponible per centrar-ho bé
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Llista d'activitats per pintar-les automàticament
                    val items = listOf(
                        Triple("Walking", Icons.Default.DirectionsWalk, PrimaryBlue),
                        Triple("Eating", Icons.Default.Restaurant, SecondaryGreen),
                        Triple("Medication", Icons.Default.Medication, PrimaryBlue),
                        Triple("Sleep", Icons.Default.Bed, SecondaryGreen),
                        Triple("Hydration", Icons.Default.LocalDrink, PrimaryBlue),
                        Triple("Exercise", Icons.Default.FitnessCenter, SecondaryGreen),
                        Triple("Depositions", Icons.Default.MusicNote, Color.Gray)
                    )

                    val radius = 130.dp // Mida del cercle (radi)

                    // Bucle per crear cada bombolla en la posició exacta del cercle
                    items.forEachIndexed { index, item ->
                        // Calculem l'angle. -PI/2 fa que el primer ítem comenci a dalt de tot (les 12h)
                        val angleRad = (2 * Math.PI * index / items.size) - (Math.PI / 2)

                        // Matemàtiques per trobar la X i la Y
                        val xOffset = (radius.value * cos(angleRad)).dp
                        val yOffset = (radius.value * sin(angleRad)).dp

                        ActivityBubble(
                            text = item.first,
                            icon = item.second,
                            color = item.third,
                            modifier = Modifier.offset(x = xOffset, y = yOffset),
                            onClick = { onActivityClick(item.first.lowercase()) }
                        )
                    }

                    // (Opcional) Icona central decorativa fantasma
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = PrimaryBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}

// --- COMPONENTS AUXILIARS ---

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
        Surface(
            shape = RoundedCornerShape(12.dp), // Una mica més arrodonit
            color = PrimaryBlue,
            modifier = Modifier.size(48.dp).shadow(4.dp, RoundedCornerShape(12.dp))
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
        Column {
            Text(
                text = "Hola, $nomAMostrar",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextGrey
            )
            Text(
                text = "Com et trobes avui?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextGrey.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ActivityBubble(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Botó rodó amb ombra i estil
    Surface(
        modifier = modifier
            .size(85.dp) // Mida de la bombolla
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = color.copy(alpha = 0.4f) // L'ombra té un toc del color del botó
            ),
        shape = CircleShape,
        color = color,
        onClick = onClick // Fa l'efecte "ripple" en clicar
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
        shadowElevation = 16.dp // Més ombra per separar-ho del fons
    ) {
        Box {
            // Línia de color superior
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryBlue, SecondaryGreen)
                        )
                    )
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
    val scale = if (isSelected) 1.1f else 1.0f // Petit efecte d'escala si està seleccionat

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp * scale)
        )
        if (isSelected) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}