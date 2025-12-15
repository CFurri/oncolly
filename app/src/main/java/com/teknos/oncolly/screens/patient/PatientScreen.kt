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

// (Opcional) Recomanació: Fes-los privats per no barrejar-los amb altres pantalles
private val PrimaryBlue = Color(0xFF259DF4)
val SecondaryGreen = Color(0xFF66BB6A)
val TextGrey = Color(0xFF565D6D)

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

        // Contenidor principal (Column)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush) // RECOMANACIÓ: Aplicar el fons aquí
                .padding(padding)
        ) {

            HeaderPacient()

            // AQUESTA ÉS LA CAPSA DEL "SISTEMA SOLAR"
            Box(
                modifier = Modifier
                    .weight(1f) // Ocupa tot l'espai restant
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center // Tot el que hi posis anirà al centre per defecte
            ) {
                // 1. CORRECCIÓ: La icona central ara està DINS del Box
                // La posem primer perquè quedi al fons (per sota de les bombolles si es toquen)
                Icon(
                    imageVector = Icons.Default.MonitorHeart,
                    contentDescription = null,
                    tint = PrimaryBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp) // Mida gran
                )

                // 2. Les bombolles al voltant
                val items = listOf(
                    Triple(stringResource(R.string.walking_patient_screen), Icons.Default.DirectionsWalk, PrimaryBlue),
                    Triple(stringResource(R.string.eating_patient_screen), Icons.Default.Restaurant, SecondaryGreen),
                    Triple(stringResource(R.string.medication_patient_screen), Icons.Default.Medication, PrimaryBlue),
                    Triple(stringResource(R.string.sleep_patient_screen), Icons.Default.Bed, SecondaryGreen),
                    Triple(stringResource(R.string.hydration_patient_screen), Icons.Default.LocalDrink, PrimaryBlue),
                    Triple(stringResource(R.string.exercise_patient_screen), Icons.Default.FitnessCenter, SecondaryGreen),
                    Triple(stringResource(R.string.depositions_patient_screen), Icons.Default.Bathroom, PrimaryBlue),
                    Triple(stringResource(R.string.upload_png_patient_screen), Icons.Default.CloudUpload, SecondaryGreen)
                )

                val radius = 130.dp

                items.forEachIndexed { index, item ->
                    // Càlcul de l'angle per fer el cercle
                    val angleRad = (2 * Math.PI * index / items.size) - (Math.PI / 2)

                    val xOffset = (radius.value * cos(angleRad)).dp
                    val yOffset = (radius.value * sin(angleRad)).dp

                    ActivityBubble(
                        text = item.first,
                        icon = item.second,
                        color = item.third,
                        modifier = Modifier.offset(x = xOffset, y = yOffset), // Això les mou des del centre cap a fora
                        onClick = {
                            if (item.first == context.getString(R.string.upload_png_patient_screen)) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://bucket-projecte-ocr-sang-final-per-web-statica.s3-website-eu-west-1.amazonaws.com")
                                )
                                context.startActivity(intent)
                            } else {
                                onActivityClick(item.first.lowercase())
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
            shape = RoundedCornerShape(12.dp),
            color = PrimaryBlue,
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
    val color = if (isSelected) PrimaryBlue else Color.Gray

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