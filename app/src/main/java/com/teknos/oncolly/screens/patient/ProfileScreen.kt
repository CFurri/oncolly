package com.teknos.oncolly.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.singletons.SingletonApp


private val BackgroundRed = Color(0xFFFFEBEE)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToActivities: () -> Unit
) {
    // 1. Dades del Singleton (Estat inicial)
    val pacient = SingletonApp.getInstance().pacientActual

    // 2. Estats locals per editar (això és el que canvia l'usuari en pantalla)
    var email by remember { mutableStateOf(pacient?.email ?: "") }
    var telefon by remember { mutableStateOf(pacient?.phoneNumber ?: "") }
    var dataNaixement by remember { mutableStateOf(pacient?.dateOfBirth ?: "") }

    // 3. Mode Edició: Controla si estem mirant o escrivint
    var isEditing by remember { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color.White, PrimaryBlue.copy(alpha = 0.15f))
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentTab = 2,
                onNavigateToHome = onNavigateToHome,
                onNavigateToActivities = onNavigateToActivities,
                onNavigateToProfile = {}
            )
        },
        // AFEGIM UN BOTÓ FLOTANT PER EDITAR / GUARDAR
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isEditing) {
                        // --- AQUÍ ÉS ON GUARDARÍEM AL SERVIDOR ---
                        // TODO: Cridar api.updatePatient(...)

                        // Per ara, simulem que es guarda i tornem a mode lectura
                        isEditing = false
                    } else {
                        // Entrem en mode edició
                        isEditing = true
                    }
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                // Canviem la icona segons l'estat
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Guardar" else "Editar"
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditing) "Editant Perfil" else "El meu Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Avatar (Igual que abans)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(120.dp).shadow(10.dp, CircleShape)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().background(PrimaryBlue.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = PrimaryBlue)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- TARGETA DE DADES (ADAPTABLE) ---
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Si estem editant -> TextField. Si no -> Text normal
                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Email,
                            label = "Correu Electrònic",
                            value = email,
                            onValueChange = { email = it }
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Phone,
                            label = "Telèfon",
                            value = telefon,
                            onValueChange = { telefon = it },
                            keyboardType = KeyboardType.Phone
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Today,
                            label = "Data de Naixement",
                            value = dataNaixement,
                            onValueChange = { dataNaixement = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))

                // Botó Logout (Només visible si NO estem editant per seguretat)
                if (!isEditing) {
                    Button(
                        onClick = {
                            SingletonApp.getInstance().tancarSessio()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BackgroundRed,
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tancar Sessió", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- NOU COMPONENT INTEL·LIGENT ---
// Decideix si mostra un Text o un TextField segons "isEditing"
@Composable
fun EditableProfileItem(
    isEditing: Boolean,
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icona
        Surface(
            shape = CircleShape, color = PrimaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)

            if (isEditing) {
                // VERSÓ EDITABLE
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextGrey),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryBlue
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
                )
            } else {
                // VERSIÓ LECTURA
                Text(
                    text = value.ifBlank { "No definit" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGrey,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}