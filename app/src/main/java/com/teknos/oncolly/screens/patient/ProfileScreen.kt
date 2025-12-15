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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teknos.oncolly.R
import com.teknos.oncolly.entity.UpdatePatientRequest
import com.teknos.oncolly.screens.doctor.PrimaryBlue
import com.teknos.oncolly.singletons.SingletonApp
import kotlinx.coroutines.launch

private val BackgroundRed = Color(0xFFFFEBEE)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToActivities: () -> Unit
) {
    // 1. Dades del Singleton (Estat inicial)
    val app = SingletonApp.getInstance()
    // 2. Estats locals per editar (això és el que canvia l'usuari en pantalla)
    var firstName by remember { mutableStateOf(app.pacientActual?.firstName ?: "") }
    var lastName by remember { mutableStateOf(app.pacientActual?.lastName ?: "") }
    var email by remember { mutableStateOf(app.pacientActual?.email ?: "") }
    var telefon by remember { mutableStateOf(app.pacientActual?.phoneNumber ?: "") }
    var dataNaixement by remember { mutableStateOf(app.pacientActual?.dateOfBirth ?: "") }

    var isLoading by remember { mutableStateOf(true) }
    // 3. Mode Edició: Controla si estem mirant o escrivint
    var isEditing by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            // Cridem al NOU endpoint que no necessita ID
            val token = "Bearer ${app.userToken}"
            val response = app.api.getPacientProfile(token)

            if (response.isSuccessful && response.body() != null) {
                val dadesFresques = response.body()!!

                // Actualitzem les variables de la pantalla
                firstName = dadesFresques.firstName
                lastName = dadesFresques.lastName
                email = dadesFresques.email
                telefon = dadesFresques.phoneNumber ?: ""
                dataNaixement = dadesFresques.dateOfBirth ?: ""

                // També actualitzem el Singleton per si de cas
                app.pacientActual = dadesFresques
            }
        } catch (e: Exception) {
            // Gestionar error si cal (ex: sense internet)
        } finally {
            isLoading = false
        }
    }

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
                        scope.launch {
                            try {
                                val token = "Bearer ${app.userToken}"
                                val req = UpdatePatientRequest(firstName, lastName, email, telefon, dataNaixement)
                                val resp = app.api.updatePatientProfile(token, req)
                                if (resp.isSuccessful) {
                                    isEditing = false
                                    // Update Singleton
                                    app.pacientActual = app.pacientActual?.copy(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        phoneNumber = telefon,
                                        dateOfBirth = dataNaixement
                                    )
                                    android.widget.Toast.makeText(context,
                                        context.getString(R.string.perfil_actualitzat_ProfileScreen), android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Error: ${resp.code()}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch(e: Exception) {
                                android.widget.Toast.makeText(context,
                                    context.getString(R.string.error_connexio_ProfileScreen), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
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
                    text = if (isEditing) stringResource(R.string.editant_perfil_ProfileScreen) else stringResource(
                        R.string.el_meu_perfil_ProfileScreen
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Avatar (Igual que abans)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(10.dp, CircleShape)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryBlue.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = if (firstName.isNotEmpty()) firstName.first().toString().uppercase() else "P",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
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

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.nom_ProfileScreen),
                            value = firstName,
                            onValueChange = { firstName = it }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.cognom_ProfileScreen),
                            value = lastName,
                            onValueChange = { lastName = it }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.correu_electr_nic_ProfileScreen),
                            value = email,
                            onValueChange = { email = it }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Phone,
                            label = stringResource(R.string.tel_fon_ProfileScreen),
                            value = telefon,
                            onValueChange = { telefon = it },
                            keyboardType = KeyboardType.Phone
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        EditableProfileItem(
                            isEditing = isEditing,
                            icon = Icons.Default.Today,
                            label = stringResource(R.string.data_de_naixement_ProfileScreen),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.tancar_sessio_ProfileScreen), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                    text = value.ifBlank { stringResource(R.string.no_definit_profilescreen) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGrey,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}