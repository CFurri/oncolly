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

private val RedLogout = Color(0xFFFF5252)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToActivities: () -> Unit
) {
    // 1. Dades(Estat inicial)
    val app = SingletonApp.getInstance()
    // 2. Estats locals per editar
    var firstName by remember { mutableStateOf(app.pacientActual?.firstName ?: "") }
    var lastName by remember { mutableStateOf(app.pacientActual?.lastName ?: "") }
    var email by remember { mutableStateOf(app.pacientActual?.email ?: "") }
    var telefon by remember { mutableStateOf(app.pacientActual?.phoneNumber ?: "") }
    var dataNaixement by remember { mutableStateOf(app.pacientActual?.dateOfBirth ?: "") }

    // 3. Mode Edició
    var isEditing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val token = "Bearer ${app.userToken}"
            val response = app.api.getPacientProfile(token)

            if (response.isSuccessful && response.body() != null) {
                val dadesFresques = response.body()!!
                firstName = dadesFresques.firstName
                lastName = dadesFresques.lastName
                email = dadesFresques.email
                telefon = dadesFresques.phoneNumber ?: ""
                dataNaixement = dadesFresques.dateOfBirth ?: ""
                app.pacientActual = dadesFresques
            }
        } catch (e: Exception) {
            // Gestió d'errors silenciosa o Toast
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color.White, PrimaryBlue.copy(alpha = 0.15f))
    )

    // FUNCIÓ PER GUARDAR (Extreta per reutilitzar-la al botó de dalt)
    fun saveProfile() {
        scope.launch {
            try {
                val token = "Bearer ${app.userToken}"
                val req = UpdatePatientRequest(firstName, lastName, email, telefon, dataNaixement)
                val resp = app.api.updatePatientProfile(token, req)
                if (resp.isSuccessful) {
                    isEditing = false
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
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentTab = 2,
                onNavigateToHome = onNavigateToHome,
                onNavigateToActivities = onNavigateToActivities,
                onNavigateToProfile = {}
            )
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

                // --- CAPÇALERA AMB TÍTOL I BOTÓ EDITAR  ---
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Títol centrat
                    Text(
                        text = if (isEditing) stringResource(R.string.editant_perfil_ProfileScreen) else stringResource(R.string.el_meu_perfil_ProfileScreen),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextGrey,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // --- BOTÓ ---
                    Surface(
                        onClick = {
                            if (isEditing) saveProfile() else isEditing = true
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEditing) SecondaryGreen else PrimaryBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Guardar" else "Editar",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Avatar
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

                // --- TARGETA DE DADES ---
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

                // BOTÓ LOGOUT
                if (!isEditing) {
                    Button(
                        onClick = {
                            SingletonApp.getInstance().tancarSessio()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedLogout.copy(alpha = 0.9f)),
                        // Shape per defecte o una mica arrodonit
                        shape = ButtonDefaults.shape,
                        modifier = Modifier
                            .fillMaxWidth(0.6f) // 60% d'amplada
                            .height(50.dp),
                        elevation = ButtonDefaults.buttonElevation(2.dp)
                    ) {
                        Text(stringResource(R.string.tancar_sessio_ProfileScreen), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Espai extra al final perquè no quedi enganxat a la barra de navegació
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

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