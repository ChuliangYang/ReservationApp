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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
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
import com.example.reservationapp.core.ui.ScheduleList
import com.example.reservationapp.data.model.ReserveBlockLength
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientSideProviderScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAvailableTimeSlots: (
        providerId: Int,
        date: LocalDate,
        length: ReserveBlockLength,
    ) -> Unit,
    viewModel: ClientSideProviderScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.onEach {
            it?.let { event ->
                when (event) {
                    is ClientSideProviderScheduleEvent.Warning -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }

                    is ClientSideProviderScheduleEvent.NavigateToAvailableTimeSlot -> {
                        onNavigateToAvailableTimeSlots(event.providerId, event.date, event.length)
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
                    Text("Make Reservation")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
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
            FloatingActionButton(onClick = {
                resetReserveCreationState()
                // Set the state to show the reserve length dialog
                showDatePicker = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Reserve")
            }
        }
    ) { paddingValues ->
        when (val uiState = uiState) {
            is ClientSideProviderScheduleUIState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ClientSideProviderScheduleUIState.Error -> {
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

            is ClientSideProviderScheduleUIState.ShowProviderSchedule -> {
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
                    ScheduleList(scheduleList = uiState.schedule)
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
        val reservingState = uiState as? ClientSideProviderScheduleUIState.ShowProviderSchedule
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    datePickerState: DatePickerState = rememberDatePickerState(),
    onDismiss: () -> Unit,
    onConfirm: (selectedTimeStamp: Long?) -> Unit,
) {
    androidx.compose.material3.DatePickerDialog(
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