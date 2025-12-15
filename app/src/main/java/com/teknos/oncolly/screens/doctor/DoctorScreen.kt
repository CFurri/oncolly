package com.teknos.oncolly.screens.doctor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teknos.oncolly.R
import com.teknos.oncolly.entity.Appointment
import com.teknos.oncolly.entity.Pacient
import com.teknos.oncolly.singletons.SingletonApp
import com.teknos.oncolly.utils.PdfGenerator
import com.teknos.oncolly.viewmodel.AppointmentUiState
import com.teknos.oncolly.viewmodel.AppointmentViewModel
import com.teknos.oncolly.viewmodel.DoctorViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// --- PALETA DE COLORS UNIFICADA ---
val PrimaryBlue = Color(0xFF259DF4)
val SecondaryGreen = Color(0xFF66BB6A)
val TextDark = Color(0xFF2C3E50)
val TextGrey = Color(0xFF7F8C8D)
val BgLight = Color(0xFFF8F9FA)

enum class DoctorTab(val icon: ImageVector, val title: String) {
    PACIENTS(Icons.Outlined.Home, "Pacients"),
    AGENDA(Icons.Outlined.DateRange, "Agenda"),
    PERFIL(Icons.Outlined.Person, "Perfil")
}

@Composable
fun DoctorScreen(
    onLogout: () -> Unit,
    onPacientClick: (String) -> Unit
) {
    val doctorViewModel: DoctorViewModel = viewModel()
    val appointmentViewModel: AppointmentViewModel = viewModel()

    var selectedTab by remember { mutableStateOf(DoctorTab.PACIENTS) }
    var showAddPatientDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        doctorViewModel.loadPatients()
        appointmentViewModel.loadAppointments()
    }

    Scaffold(
        containerColor = BgLight,
        bottomBar = {
            DoctorBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            if (selectedTab == DoctorTab.PACIENTS) {
                FloatingActionButton(
                    onClick = { showAddPatientDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Patient")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            
            // ANIMATED CONTENT FOR TABS
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "DoctorTabs"
            ) { tab ->
                when (tab) {
                    DoctorTab.PACIENTS -> PantallaLlistaPacients(
                        totsElsPacients = doctorViewModel.state.patients,
                        onPacientClick = onPacientClick,
                        onDeletePacient = { id -> doctorViewModel.deletePatient(id) }
                    )
                    DoctorTab.AGENDA -> AgendaScreen(
                        state = appointmentViewModel.state,
                        patients = doctorViewModel.state.patients,
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
    
    // Feedback and Errors
    if (doctorViewModel.state.error != null) {
        AlertDialog(
            onDismissRequest = { doctorViewModel.clearError() },
            confirmButton = { 
                TextButton(onClick = { doctorViewModel.clearError() }) { 
                    Text("OK", color = PrimaryBlue) 
                } 
            },
            title = { Text("Error") },
            text = { Text(doctorViewModel.state.error ?: "Unknown error") },
            containerColor = Color.White
        )
    }

    if (showAddPatientDialog) {
        AddPatientDialog(
            onDismiss = { showAddPatientDialog = false },
            onSubmit = { first, last, email, password, phone, dob ->
                doctorViewModel.createPatient(first, last, email, password, phone, dob) {
                    showAddPatientDialog = false
                    PdfGenerator.generateAndSharePatientPdf(context, first, last, email, password)
                }
            }
        )
    }
}

// --- BARRA DE NAVEGACIÓ PERSONALITZADA ---
@Composable
fun DoctorBottomBar(
    selectedTab: DoctorTab,
    onTabSelected: (DoctorTab) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            // Línia gradient superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.horizontalGradient(listOf(PrimaryBlue, SecondaryGreen)))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DoctorTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val color = if (isSelected) PrimaryBlue else Color.LightGray
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = color,
                            modifier = Modifier.size(26.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                color = color,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- SUB-PANTALLA 1: LLISTA AMB BUSCADOR ---
@Composable
fun PantallaLlistaPacients(
    totsElsPacients: List<Pacient>,
    onPacientClick: (String) -> Unit,
    onDeletePacient: (String) -> Unit
) {
    var textBuscador by remember { mutableStateOf("") }

    val pacientsFiltrats = if (textBuscador.isEmpty()) {
        totsElsPacients
    } else {
        totsElsPacients.filter { 
            val fullName = "${it.firstName} ${it.lastName}"
            fullName.contains(textBuscador, ignoreCase = true) || it.email.contains(textBuscador, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.pacients_title_DoctorScreen), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textBuscador,
            onValueChange = { textBuscador = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.buscar_per_nom_o_email_DoctorScreen), color = TextGrey) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextGrey) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = PrimaryBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(pacientsFiltrats) { pacient ->
                ItemPacientDisseny(pacient, onPacientClick, onDeletePacient)
            }
        }
    }
}

@Composable
fun ItemPacientDisseny(pacient: Pacient, onClick: (String) -> Unit, onDelete: (String) -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp), // Minimalista (flat)
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(pacient.id) }
            // Afegim una mica de vora subtil
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (pacient.firstName.isNotEmpty()) pacient.firstName.take(1).uppercase() else "P",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pacient.firstName} ${pacient.lastName}", 
                    fontWeight = FontWeight.SemiBold, 
                    fontSize = 16.sp, 
                    color = TextDark
                )
                Text(text = pacient.email, color = TextGrey, fontSize = 12.sp)
            }

            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.eliminar_pacient_DoctorScreen)) },
            text = { Text(
                stringResource(
                    R.string.est_s_segur_que_vols_eliminar__DoctorScreen,
                    pacient.firstName
                )) },
            confirmButton = {
                TextButton(onClick = { 
                    onDelete(pacient.id)
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.eliminar_button_DoctorScreen), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel_lar_button__DoctorScreen)) }
            },
            containerColor = Color.White
        )
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
    var draftTitle by remember { mutableStateOf("Visita seguiment") }
    var draftNotes by remember { mutableStateOf("") }
    var draftStart by remember { mutableStateOf(now) }
    var draftDurationMinutes by remember { mutableStateOf(30) } 
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
                text = { Text(stringResource(R.string.nova_cita_DoctorScreen)) },
                containerColor = SecondaryGreen,
                contentColor = Color.White
            )
        },
        containerColor = BgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.agenda_title__DoctorScreen), fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextDark)
                IconButton(onClick = onReload) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = PrimaryBlue)
                }
            }
            
            if (state.isLoading && state.appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (state.appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.sense_cites_programades_DoctorScreen), color = TextGrey)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    state.appointments
                        .sortedBy { it.startTime }
                        .groupBy { runCatching { LocalDateTime.parse(it.startTime).toLocalDate() }.getOrNull() }
                        .forEach { (day, itemsForDay) ->
                            item(key = "header-$day") {
                                Text(
                                    text = day?.let { dayFormatter.format(it) } ?: "Data desconeguda",
                                    fontWeight = FontWeight.Bold,
                                    color = TextGrey,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
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

            state.feedback?.let {
                LaunchedEffect(it) {
                    delay(2000)
                    appointmentViewModel.clearFeedback()
                }
            }
        }
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
                    val end = draftStart.plusMinutes(draftDurationMinutes.toLong())
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
    
    val startTimeStr = start?.let { timeFormatter.format(it) } ?: "--:--"
    val endTimeStr = end?.let { timeFormatter.format(it) } ?: "--:--"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(BgLight, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(startTimeStr, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                Text(endTimeStr, color = TextGrey, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                appointment.patientName?.let { 
                    Text(it, color = PrimaryBlue, fontSize = 13.sp) 
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Esborrar", 
                    tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
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
    durationMinutes: Int,
    onStartChange: (LocalDateTime) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    sheetState: SheetState
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var patientMenuExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    
    val startDateState = rememberDatePickerState(initialSelectedDateMillis = startTime.toEpochMillis())
    val startTimeState = rememberTimePickerState(initialHour = startTime.hour, initialMinute = startTime.minute, is24Hour = true)

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nova Cita", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextDark)

            // SELECTOR DE PACIENT MILLORAT (Searchable)
            Box {
                val selectedPatientName = patients.find { it.id == selectedPatient }?.let { "${it.firstName} ${it.lastName}" } ?: "Selecciona pacient"
                
                OutlinedTextField(
                    value = selectedPatientName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { patientMenuExpanded = true },
                    label = { Text(stringResource(R.string.pacient_DoctorScreen)) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    enabled = false, 
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = TextDark,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = TextGrey,
                        disabledTrailingIconColor = TextGrey
                    )
                )
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable { patientMenuExpanded = true })
                
                DropdownMenu(
                    expanded = patientMenuExpanded,
                    onDismissRequest = { patientMenuExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 300.dp)
                        .background(Color.White)
                ) {
                    // Search Bar dins del menú
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text(stringResource(R.string.buscar_DoctorScreen)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        singleLine = true
                    )
                    
                    val filteredPatients = patients.filter { 
                        "${it.firstName} ${it.lastName}".contains(searchText, ignoreCase = true) || it.email.contains(searchText, ignoreCase = true) 
                    }
                    
                    if (filteredPatients.isEmpty()) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.cap_resultat_DoctorScreen), color = TextGrey) }, onClick = {})
                    } else {
                        filteredPatients.forEach { pacient ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text("${pacient.firstName} ${pacient.lastName}", fontWeight = FontWeight.Bold)
                                        Text(pacient.email, fontSize = 12.sp, color = TextGrey)
                                    }
                                },
                                onClick = {
                                    onPatientChange(pacient.id)
                                    patientMenuExpanded = false
                                    searchText = "" // Reset search
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.t_tol_DoctorScreen)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Data i Hora
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier
                    .weight(1f)
                    .clickable { showStartDatePicker = true }) {
                    OutlinedTextField(
                        value = dateFormatter.format(startTime),
                        onValueChange = {},
                        label = { Text(
                            stringResource(R.string.diaDiaa_DoctorScreen) +
                                "a") },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextDark, disabledBorderColor = Color.LightGray, disabledLabelColor = TextGrey, disabledLeadingIconColor = PrimaryBlue
                        )
                    )
                }
                Box(modifier = Modifier
                    .weight(1f)
                    .clickable { showStartTimePicker = true }) {
                    OutlinedTextField(
                        value = timeFormatter.format(startTime),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.hora_DoctorScreen)) },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Schedule, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextDark, disabledBorderColor = Color.LightGray, disabledLabelColor = TextGrey, disabledLeadingIconColor = PrimaryBlue
                        )
                    )
                }
            }

            // Durada
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgLight, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(stringResource(R.string.durada_min_DoctorScreen), color = TextGrey, fontWeight = FontWeight.Medium)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (durationMinutes > 15) onDurationChange(durationMinutes - 15) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = PrimaryBlue)
                    }
                    Text(
                        text = "$durationMinutes",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                    IconButton(
                        onClick = { onDurationChange(durationMinutes + 15) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = PrimaryBlue)
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(R.string.notes_opcional_DoctorScreen)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.guardar_cita_DoctorScreen), fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateState.selectedDateMillis?.let { millis ->
                        val date = millis.toLocalDate()
                        onStartChange(startTime.withDate(date))
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = PrimaryBlue) }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text(
                stringResource(R.string.cancel_DoctorScreen)
            ) } }
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
            TimePicker(state = startTimeState)
        }
    }
}

// --- PANTALLA ADD PATIENT DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf(LocalDate.now().minusYears(30)) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = dob.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    AlertDialog(
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nou_pacient_DoctorScreen), fontWeight = FontWeight.Bold, color = TextDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName, onValueChange = { firstName = it }, label = { Text(
                            stringResource(R.string.nom_DoctorScreen)
                        ) },
                        singleLine = true, modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue)
                    )
                    OutlinedTextField(
                        value = lastName, onValueChange = { lastName = it }, label = { Text(
                            stringResource(R.string.cognom_DoctorScreen)
                        ) },
                        singleLine = true, modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue)
                    )
                }
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text(stringResource(R.string.email_DoctorScreen)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue
                    )
                )
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text(stringResource(R.string.password_DoctorScreen)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue
                    )
                )
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { phone = it }, 
                    label = { Text(stringResource(R.string.tel_fon_DoctorScreen)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue, focusedLabelColor = PrimaryBlue
                    )
                )
                
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = dob.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.data_naixement_DoctorScreen)) },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryBlue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextDark, disabledBorderColor = Color.LightGray, disabledLabelColor = TextGrey
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(firstName, lastName, email, password, phone, dob.format(DateTimeFormatter.ISO_LOCAL_DATE)) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(stringResource(R.string.crear__DoctorScreen))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_lar_DoctorScreen), color = TextGrey)
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        dob = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = PrimaryBlue) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dateState)
        }
    }
}


// --- UTILITATS DATA ---
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        containerColor = Color.White,
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK", color = PrimaryBlue) } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel", color = TextGrey) } },
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
    val doctor = SingletonApp.getInstance().doctorActual
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = doctor?.firstName?.take(1)?.uppercase() ?: "D",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "${doctor?.firstName ?: stringResource(R.string.doctor_DoctorScreen)} ${doctor?.lastName ?: ""}",
            fontSize = 24.sp, 
            fontWeight = FontWeight.Bold, 
            color = TextDark
        )
        Text(
            text = doctor?.specialization ?: stringResource(R.string.especialitat_desconeguda_DoctorScreen),
            fontSize = 16.sp, 
            color = SecondaryGreen,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = doctor?.email ?: "", 
            fontSize = 14.sp, 
            color = TextGrey
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLogout, 
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.9f)),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(stringResource(R.string.tancar_sessio_DoctorScreen))
        }
    }
}