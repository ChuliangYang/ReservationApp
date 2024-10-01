package com.example.reservationapp.ui.feature.clientReservation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reservationapp.core.ScheduleList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientReservationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClientReservationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.onEach {
            it?.let { event ->
                when (event) {
                    is ClientReservationEventState.Warning -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.onEventHandled()
            }
        }.collect()
    }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showReserveLengthDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }

    fun resetReserveCreationState() {
        showDatePicker = false
        showReserveLengthDialog = false
        selectedDate = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (uiState) {
                        is ClientReservationUIState.ShowProviderSchedule -> {
                            Text("Make Reservation")
                        }

                        is ClientReservationUIState.ShowAvailableTimeSlots -> {
                            Text("Available Time Slots")
                        }

                        is ClientReservationUIState.Reserved -> {
                            Text("Reservation Details")
                        }

                        else -> {
                            Text("Reservation")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (uiState) {
                            is ClientReservationUIState.ShowAvailableTimeSlots -> {
                                viewModel.refresh()
                            }

                            else -> {
                                onNavigateBack()
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (uiState is ClientReservationUIState.ShowProviderSchedule) {
                FloatingActionButton(onClick = {
                    resetReserveCreationState()
                    // Set the state to show the reserve length dialog
                    showDatePicker = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Reserve")
                }
            }
        }
    ) { paddingValues ->
        when (val uiState = uiState) {
            is ClientReservationUIState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ClientReservationUIState.Error -> {
                val errorMessage = uiState.message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }

            is ClientReservationUIState.ShowProviderSchedule -> {
                val reservingState = uiState as ClientReservationUIState.ShowProviderSchedule
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = "Provider Schedule Detail",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    )
                    ScheduleList(scheduleList = reservingState.schedule)
                }
            }

            is ClientReservationUIState.ShowAvailableTimeSlots -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    items(uiState.availableTimeSlots) {
                        ListItem(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            headlineContent = {
                                Text(
                                    text = "${it.startTime.format(DateTimeFormatter.ISO_LOCAL_TIME)} ~ ${
                                        it.endTime.format(
                                            DateTimeFormatter.ISO_LOCAL_TIME
                                        )
                                    }"
                                )
                            }, trailingContent = {
                                Button(
                                    onClick = {
                                        viewModel.onTimeSlotSelected(it)
                                    }
                                ) {
                                    Text(text = "Reserve")
                                }
                            })
                        HorizontalDivider()
                    }
                }
            }

            is ClientReservationUIState.Reserved -> {
                val reservation = uiState.reservation
                var showDeleteDialog by remember { mutableStateOf(false) }
                var showConfirmDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Display reservation details
                    ReservationDetails(reservation)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Confirm Reservation Button
                    if (!reservation.isConfirmed) {
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Confirm Reservation")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Delete Reservation Button
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "Delete Reservation")
                    }
                }

                if (showConfirmDialog) {
                    ConfirmDialog(
                        title = "Confirm Reservation",
                        message = "Are you sure you want to confirm this reservation?",
                        onConfirm = {
                            showConfirmDialog = false
                            viewModel.onReservationConfirm(reservation.id)
                        },
                        onDismiss = { showConfirmDialog = false }
                    )
                }

                // Confirmation Dialog for Deleting Reservation
                if (showDeleteDialog) {
                    ConfirmDialog(
                        title = "Delete Reservation",
                        message = "Are you sure you want to delete this reservation?",
                        onConfirm = {
                            showDeleteDialog = false
                            viewModel.onReservationDelete(reservation.id)
                        },
                        onDismiss = { showDeleteDialog = false }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val today = Instant.now().atZone(ZoneOffset.UTC)
        DatePickerDialog(
            datePickerState = rememberDatePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneOffset.UTC)
                        return date.isAfter(today)
                    }
                }
            ),
            onDismiss = {
                resetReserveCreationState()
            },
            onConfirm = {
                it?.let {
                    val date = Instant.ofEpochMilli(it)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    selectedDate = date
                    showDatePicker = false
                    showReserveLengthDialog = true
                }
            }
        )
    }

    // Reserve Length Dialog
    if (showReserveLengthDialog) {
        val reservingState = uiState as? ClientReservationUIState.ShowProviderSchedule
        AlertDialog(
            onDismissRequest = { resetReserveCreationState() },
            title = { Text("Reservation Length") },
            text = {
                Column {
                    reservingState?.reserveBlockLength?.forEach { length ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onRequestAvailableTimeSlots(selectedDate, length)
                                    resetReserveCreationState()
                                }
                                .padding(8.dp)
                        ) {
                            Text(text = "${length.lengthInMinute} minutes")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { resetReserveCreationState() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReservationDetails(reservation: ReservationUI) {
    Column {
        Text(
            text = "Date: ${reservation.date}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Time: ${reservation.timeSlot.startTime} - ${reservation.timeSlot.endTime}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Confirmed: ${if (reservation.isConfirmed) "Yes" else "No"}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    datePickerState: DatePickerState = rememberDatePickerState(),
    onDismiss: () -> Unit,
    onConfirm: (selectedTimeStamp: Long?) -> Unit,
) {
    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(datePickerState.selectedDateMillis)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
