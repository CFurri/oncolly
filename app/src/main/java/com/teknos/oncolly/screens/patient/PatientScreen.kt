package com.teknos.oncolly.screens.patient

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R
import com.teknos.oncolly.singletons.SingletonApp
import kotlin.math.cos
import kotlin.math.sin

private val PrimaryBlue = Color(0xFF259DF4)
val SecondaryGreen = Color(0xFF66BB6A)
val TextGrey = Color(0xFF565D6D)

// CLASSE AUXILIAR: Per guardar l'ID intern + el Text traduït
data class MenuOption(
    val id: String,         // ID intern (ex: "walking") -> NO CANVIA
    val label: String,      // Text visible (ex: "Caminar") -> CANVIA AMB L'IDIOMA
    val icon: ImageVector,
    val color: Color
)

@Composable
fun PacientScreen(
    onLogout: () -> Unit,
    onActivityClick: (String) -> Unit,
    onNavigateToActivitiesList: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            PrimaryBlue.copy(alpha = 0.15f)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(padding)
        ) {

            HeaderPacient()

            // --- SISTEMA SOLAR ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Icona central
                Icon(
                    imageVector = Icons.Default.MonitorHeart,
                    contentDescription = null,
                    tint = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp)
                )

                // Passem l'ID manualment perquè coincideixi amb ActivityType.kt
                val items = listOf(
                    MenuOption("walking", stringResource(R.string.walking_patient_screen), Icons.Default.DirectionsWalk, PrimaryBlue),
                    MenuOption("eating", stringResource(R.string.eating_patient_screen), Icons.Default.Restaurant, SecondaryGreen),
                    MenuOption("medication", stringResource(R.string.medication_patient_screen), Icons.Default.Medication, PrimaryBlue),
                    MenuOption("sleep", stringResource(R.string.sleep_patient_screen), Icons.Default.Bed, SecondaryGreen),
                    MenuOption("hydration", stringResource(R.string.hydration_patient_screen), Icons.Default.LocalDrink, PrimaryBlue),
                    MenuOption("exercise", stringResource(R.string.exercise_patient_screen), Icons.Default.FitnessCenter, SecondaryGreen),
                    MenuOption("depositions", stringResource(R.string.depositions_patient_screen), Icons.Default.Wc, PrimaryBlue), // Canviat Bathroom per Wc per coincidir amb l'altre fitxer si cal
                    MenuOption("upload_png", stringResource(R.string.upload_png_patient_screen), Icons.Default.CloudUpload, SecondaryGreen)
                )

                val radius = 130.dp

                items.forEachIndexed { index, item ->
                    val angleRad = (2 * Math.PI * index / items.size) - (Math.PI / 2)
                    val xOffset = (radius.value * cos(angleRad)).dp
                    val yOffset = (radius.value * sin(angleRad)).dp

                    ActivityBubble(
                        text = item.label, // Mostrem el text traduït
                        icon = item.icon,
                        color = item.color,
                        modifier = Modifier.offset(x = xOffset, y = yOffset),
                        onClick = {
                            if (item.id == "upload_png") {
                                // Lògica especial per la web
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://bucket-projecte-ocr-sang-final-per-web-statica.s3-website-eu-west-1.amazonaws.com")
                                )
                                context.startActivity(intent)
                            } else {
                                // AQUÍ ESTÀ EL FIX: Passem l'ID intern ("walking"), no el text ("Caminar")
                                onActivityClick(item.id)
                            }
                        }
                    )
                }
            }
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
    Surface(
        modifier = modifier
            .size(85.dp)
            .shadow(8.dp, CircleShape),
        shape = CircleShape,
        color = color,
        onClick = onClick
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
            // Text petit amb maxLines per si la traducció és llarga
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 12.sp,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun HeaderPacient() {
    val app = SingletonApp.getInstance()
    val pacient = app.pacientActual
    // Fem servir takeIf per seguretat
    val nomAMostrar = pacient?.firstName?.takeIf { it.isNotEmpty() } ?: "Pacient"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF259DF4),
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MonitorHeart,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = stringResource(R.string.hola_salutacio_patient_screen, nomAMostrar),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextGrey
            )
            Text(
                text = stringResource(R.string.com_et_trobes_avui_patient_screen),
                fontSize = 14.sp,
                color = TextGrey.copy(alpha = 0.8f)
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
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Home,
                label = stringResource(R.string.home_patient_screen),
                isSelected = currentTab == 0,
                onClick = onNavigateToHome
            )
            BottomNavItem(
                icon = Icons.Outlined.Assignment,
                label = stringResource(R.string.activities_patient_screen),
                isSelected = currentTab == 1,
                onClick = onNavigateToActivities
            )
            BottomNavItem(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.profile_patient_screen),
                isSelected = currentTab == 2,
                onClick = onNavigateToProfile
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) Color(0xFF259DF4) else Color.Gray

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
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}