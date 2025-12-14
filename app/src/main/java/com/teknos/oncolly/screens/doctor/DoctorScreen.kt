package com.teknos.oncolly.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teknos.oncolly.entity.Appointment
import com.teknos.oncolly.entity.Pacient // Assegura't que l'import és correcte
import com.teknos.oncolly.singletons.SingletonApp
import com.teknos.oncolly.viewmodel.AppointmentUiState
import com.teknos.oncolly.viewmodel.AppointmentViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Definim les 3 opcions del menú inferior
enum class DoctorTab(val icon: ImageVector, val title: String) {
    PACIENTS(Icons.Default.Home, "Pacients"),
    AGENDA(Icons.Default.DateRange, "Agenda"),
    PERFIL(Icons.Default.Person, "Perfil")
}

@Composable
fun DoctorScreen(
    onLogout: () -> Unit,
    onPacientClick: (String) -> Unit
) {
    val appointmentViewModel: AppointmentViewModel = viewModel()

    // Estat per saber quina pestanya tenim seleccionada (per defecte PACIENTS)
    var selectedTab by remember { mutableStateOf(DoctorTab.PACIENTS) }

    // Estat per guardar la llista que ve del servidor
    var llistaPacients by remember { mutableStateOf<List<Pacient>>(emptyList()) }
    var errorServidor by remember { mutableStateOf<String?>(null) }

    // --- CONNEXIÓ AL SERVIDOR ---
    LaunchedEffect(Unit) {
        try {
            val api = SingletonApp.getInstance().api
            // Recuperem el token que hem guardat al login
            val token = "Bearer ${SingletonApp.getInstance().userToken}"

            val resposta = api.getPacients(token)

            if (resposta.isSuccessful) {
                llistaPacients = resposta.body() ?: emptyList()
            } else {
                errorServidor = "Error: ${resposta.code()}"
            }

            appointmentViewModel.loadAppointments()
        } catch (e: Exception) {
            errorServidor = "Error de connexió"
            println(e.message)
        }
    }

    // Aquesta és l'estructura base de la pantalla
    Scaffold(
        // 1. BARRA INFERIOR (NAVIGATION UI)
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                DoctorTab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6200EE),
                            indicatorColor = Color(0xFFE3F2FD)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // 2. CONTINGUT PRINCIPAL (Canvia segons la pestanya)
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                DoctorTab.PACIENTS -> PantallaLlistaPacients(llistaPacients,
                    onPacientClick as (String) -> Unit
                )
                DoctorTab.AGENDA -> AgendaScreen(
                    state = appointmentViewModel.state,
                    patients = llistaPacients,
                    onReload = { appointmentViewModel.loadAppointments() },
                    onCreate = { patientId, start, end, title, notes ->
                        appointmentViewModel.createAppointment(patientId, start, end, title, notes)
                    },
                    onDelete = { id -> appointmentViewModel.deleteAppointment(id) }
                )
                DoctorTab.PERFIL -> PantallaPerfilDoctor(onLogout)
            }
        }
    }
}

// --- SUB-PANTALLA 1: LLISTA AMB BUSCADOR ---
@Composable
fun PantallaLlistaPacients(
    totsElsPacients: List<Pacient>,
    onPacientClick: (String) -> Unit
) {
    // Estat del text del buscador
    var textBuscador by remember { mutableStateOf("") }

    // Lògica de filtratge (Busquem per nom)
    val pacientsFiltrats = if (textBuscador.isEmpty()) {
        totsElsPacients
    } else {
        totsElsPacients.filter { it.email.contains(textBuscador, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text("Els teus Pacients", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
        Spacer(modifier = Modifier.height(16.dp))

        // EL BUSCADOR
        OutlinedTextField(
            value = textBuscador,
            onValueChange = { textBuscador = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar per nom...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LA LLISTA (LazyColumn)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pacientsFiltrats) { pacient ->
                ItemPacientDisseny(pacient, onPacientClick)
            }
        }
    }
}

// Disseny de la targeta individual
@Composable
fun ItemPacientDisseny(pacient: Pacient, onClick: (String) -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick(pacient.id) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cercle amb la inicial (opcional, queda maco)
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFE3F2FD)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = pacient.email.first().toString().uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Dades del Pacient
            Column(modifier = Modifier.weight(1f)) {
                // Fem servir l'email com a nom principal
                Text(text = pacient.email, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // Si tenim telèfon, el mostrem. Si no, text buit.
                val telefon = pacient.phoneNumber ?: "Sense telèfon"
                Text(text = telefon, color = Color.Gray, fontSize = 14.sp)
            }

            // --- AQUI ABANS HI HAVIA LA GRAVETAT ---
            // Com que el servidor no ens la diu, de moment posem un indicador genèric
            // o l'eliminem directament.
            Text(
                text = "Actiu", // Text fix provisional
                color = Color(0xFF2E7D32), // Verd
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// --- AGENDA (DOCTOR) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    state: AppointmentUiState,
    patients: List<Pacient>,
    onReload: () -> Unit,
    onCreate: (String, LocalDateTime, LocalDateTime, String, String?) -> Unit,
    onDelete: (String) -> Unit
) {
    val dayFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val now = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    val appointmentViewModel: AppointmentViewModel = viewModel()

    var showSheet by remember { mutableStateOf(false) }
    var draftPatient by remember { mutableStateOf(patients.firstOrNull()?.id.orEmpty()) }
    var draftTitle by remember { mutableStateOf("Follow-up") }
    var draftNotes by remember { mutableStateOf("") }
    var draftStart by remember { mutableStateOf(now) }
    var draftDurationMinutes by remember { mutableStateOf("30") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(patients) {
        if (draftPatient.isEmpty() && patients.isNotEmpty()) {
            draftPatient = patients.first().id
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                text = { Text("Add") },
                containerColor = Color(0xFF0F9D58),
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF7F7F9))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Agenda", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text(
                        text = "Lightweight calendar overview",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onReload) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh agenda", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (state.isLoading && state.appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No appointments yet. Tap Add to create.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.appointments
                        .sortedBy { it.startTime }
                        .groupBy { runCatching { LocalDateTime.parse(it.startTime).toLocalDate() }.getOrNull() }
                        .forEach { (day, itemsForDay) ->
                            item(key = "header-$day") {
                                Text(
                                    text = day?.let { dayFormatter.format(it) } ?: "Unknown date",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5F6368)
                                )
                            }
                            items(itemsForDay) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    timeFormatter = timeFormatter,
                                    onDelete = { onDelete(appointment.id) }
                                )
                            }
                        }
                }
            }

            state.error?.let { /* handled via dialog below */ }
            state.feedback?.let {
                Text(text = it, color = Color(0xFF2E7D32), fontSize = 13.sp)
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(1500)
                    appointmentViewModel.clearFeedback()
                }
            }
        }
    }

    state.error?.let { message ->
        AlertDialog(
            onDismissRequest = { appointmentViewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { appointmentViewModel.clearError() }) { Text("OK") }
            },
            title = { Text("Cannot create appointment") },
            text = { Text(message.ifBlank { "Time slot unavailable or schedule is full." }) }
        )
    }

    if (showSheet) {
        AppointmentSheet(
            patients = patients,
            selectedPatient = draftPatient,
            onPatientChange = { draftPatient = it },
            title = draftTitle,
            onTitleChange = { draftTitle = it },
            notes = draftNotes,
            onNotesChange = { draftNotes = it },
            startTime = draftStart,
            durationMinutes = draftDurationMinutes,
            onStartChange = { draftStart = it },
            onDurationChange = { draftDurationMinutes = it },
            onDismiss = { showSheet = false },
            onSubmit = {
                if (draftPatient.isNotEmpty()) {
                    val duration = draftDurationMinutes.toLongOrNull() ?: 30L
                    val end = draftStart.plusMinutes(duration)
                    onCreate(draftPatient, draftStart, end, draftTitle, draftNotes.ifBlank { null })
                    showSheet = false
                    draftNotes = ""
                }
            },
            sheetState = sheetState
        )
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    timeFormatter: DateTimeFormatter,
    onDelete: () -> Unit
) {
    val start = runCatching { LocalDateTime.parse(appointment.startTime) }.getOrNull()
    val end = runCatching { LocalDateTime.parse(appointment.endTime) }.getOrNull()
    val timeLabel = if (start != null && end != null) {
        "${timeFormatter.format(start)} - ${timeFormatter.format(end)}"
    } else {
        "${appointment.startTime} → ${appointment.endTime}"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appointment.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(timeLabel, color = Color.Gray, fontSize = 13.sp)
                appointment.patientName?.let { Text(it, color = Color(0xFF1976D2), fontSize = 13.sp) }
                appointment.status?.let { Text(it, color = Color.Gray, fontSize = 12.sp) }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete appointment", tint = Color(0xFFB3261E))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentSheet(
    patients: List<Pacient>,
    selectedPatient: String,
    onPatientChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    startTime: LocalDateTime,
    durationMinutes: String,
    onStartChange: (LocalDateTime) -> Unit,
    onDurationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    sheetState: SheetState
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var patientMenuExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    val startDateState = rememberDatePickerState(
        initialSelectedDateMillis = startTime.toEpochMillis()
    )
    val startTimeState = rememberTimePickerState(
        initialHour = startTime.hour,
        initialMinute = startTime.minute,
        is24Hour = true
    )

    LaunchedEffect(startTime) {
        startDateState.selectedDateMillis = startTime.toEpochMillis()
        startTimeState.hour = startTime.hour
        startTimeState.minute = startTime.minute
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("New appointment", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            if (patients.isNotEmpty()) {
                Box {
                    OutlinedTextField(
                        value = patients.find { it.id == selectedPatient }?.email ?: "Select patient",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { patientMenuExpanded = true },
                        label = { Text("Patient") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = patientMenuExpanded,
                        onDismissRequest = { patientMenuExpanded = false }
                    ) {
                        patients.forEach { pacient ->
                            DropdownMenuItem(
                                text = { Text(pacient.email) },
                                onClick = {
                                    onPatientChange(pacient.id)
                                    patientMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text("No patients loaded", color = Color.Gray, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            DateTimeRow(
                label = "Start",
                dateLabel = dateFormatter.format(startTime),
                timeLabel = timeFormatter.format(startTime),
                onDateClick = { showStartDatePicker = true },
                onTimeClick = { showStartTimePicker = true }
            )
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = onDurationChange,
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExtendedFloatingActionButton(
                onClick = onSubmit,
                containerColor = Color(0xFF0F9D58),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.EditCalendar, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateState.selectedDateMillis?.let { millis ->
                        val date = millis.toLocalDate()
                        onStartChange(startTime.withDate(date))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = startDateState)
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onConfirm = {
                onStartChange(
                    startTime.withHour(startTimeState.hour).withMinute(startTimeState.minute)
                )
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = startTimeState, colors = TimePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                selectorColor = MaterialTheme.colorScheme.primary,
                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary
            ))
        }
    }
}

// --- SUB-PANTALLES SIMPLES (Agenda i Perfil) ---

@Composable
private fun DateTimeRow(
    label: String,
    dateLabel: String,
    timeLabel: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = onDateClick) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(dateLabel)
            }
            TextButton(onClick = onTimeClick) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(timeLabel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel") } },
        text = { content() }
    )
}

private fun LocalDateTime.toEpochMillis(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDateTime.withDate(date: LocalDate): LocalDateTime =
    date.atTime(this.toLocalTime())

@Composable
fun PantallaPerfilDoctor(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil del Doctor", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Tancar Sessió")
        }
    }
}

@Composable
fun PantallaPlaceholder(titol: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(titol, fontSize = 20.sp, color = Color.Gray)
    }
}
