import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.example.reservationapp.ui.feature.providerSchedule.ProviderEventState
import com.example.reservationapp.ui.feature.providerSchedule.ProviderScheduleViewModel
import com.example.reservationapp.ui.feature.providerSchedule.ProviderUIState
import com.example.reservationapp.ui.feature.providerSchedule.TimeSlotUIItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderScheduleScreen(
    viewModel: ProviderScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.onEach {
            it?.let {
                when (it) {
                    is ProviderEventState.Warning -> {
                        Toast.makeText(localContext, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.onEventHandled()
            }
        }.collect()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val uiState = uiState) {
                        is ProviderUIState.Scheduling -> {
                            Text("Add Your Schedule")
                        }

                        is ProviderUIState.Scheduled -> {
                            Text("Check Your Schedule")
                        }

                        else -> {
                            Text("Schedule")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            when (val uiState = uiState) {
                is ProviderUIState.Scheduling -> {
                    FloatingActionButton(
                        onClick = { viewModel.submitSchedule() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Add Schedule")
                    }
                }

                is ProviderUIState.Scheduled -> {
                    FloatingActionButton(
                        onClick = { viewModel.deleteSchedule() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Schedule")
                    }
                }

                else -> {}
            }

        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(8.dp)
                .fillMaxSize()
        ) {
            when (val currentUIState = uiState) {
                is ProviderUIState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProviderUIState.Error -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Error: ${currentUIState.message}"
                    )
                }

                is ProviderUIState.Scheduled -> {
                    ScheduledScreen(currentUIState, viewModel)
                }

                is ProviderUIState.Scheduling -> {
                    SchedulingScreen(currentUIState, viewModel)
                }
            }
        }
    }
}

@Composable
fun ScheduledScreen(
    uiState: ProviderUIState.Scheduled,
    viewModel: ProviderScheduleViewModel,
    modifier: Modifier = Modifier,
) {
    ScheduleList(uiState.schedules)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingScreen(
    uiState: ProviderUIState.Scheduling,
    viewModel: ProviderScheduleViewModel,
    modifier: Modifier = Modifier,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePickerStartDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePickerStopDialog by rememberSaveable { mutableStateOf(false) }
    var startTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Select Start Date", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showStartDatePicker = true }) {
                Text(
                    text = uiState.startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        ?: "Choose Start Date"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select End Date (Optional)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    showEndDatePicker = true
                }
            ) {
                Text(
                    text = uiState.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        ?: "Choose End Date"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Time Slots",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                showTimePickerStartDialog = true
            }) {
                Text(
                    text = "Add Time Slot",
                )
            }

            if (uiState.timeSlots.isNotEmpty()) {
                LazyColumn {
                    items(uiState.timeSlots) { slot ->
                        TimeSlotItem(
                            timeSlot = slot,
                            onRemove = { viewModel.removeTimeSlot(it) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (showStartDatePicker) {
            val today = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()
            ScheduleDatePicker(
                datePickerState = rememberDatePickerState(
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val date = Instant.ofEpochMilli(utcTimeMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            return date.isEqual(today)|| date.isAfter(today)
                        }
                    }
                ),
                onDismiss = {
                    showStartDatePicker = false
                },
                onConfirm = { selectedDate ->
                    selectedDate?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        viewModel.onStartDateSelected(selectedDate)
                        showStartDatePicker = false
                    }
                }
            )
        }

        if (showEndDatePicker) {
            val today = Instant.now().atZone(ZoneOffset.UTC)
            ScheduleDatePicker(
                datePickerState = rememberDatePickerState(
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val date = Instant.ofEpochMilli(utcTimeMillis)
                                .atZone(ZoneOffset.UTC)
                            return date.isEqual(today)|| date.isAfter(today)
                        }
                    }
                ),
                onDismiss = { showEndDatePicker = false },
                onConfirm = { selectedDate ->
                    selectedDate?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        viewModel.onEndDateSelected(selectedDate)
                        showEndDatePicker = false
                    }
                }
            )
        }

        if (showTimePickerStartDialog) {
            ScheduleTimePicker(
                title = "Select Start Time",
                onDismiss = {
                    showTimePickerStartDialog = false
                    startTime = null
                },
                onConfirm = { selectedTime ->
                    selectedTime?.let {
                        startTime = it
                        showTimePickerStartDialog = false
                        showTimePickerStopDialog = true
                    }
                },
            )
        }

        if (showTimePickerStopDialog) {
            ScheduleTimePicker(
                title = "Select Stop Time",
                onDismiss = {
                    showTimePickerStopDialog = false
                    startTime = null
                },
                onConfirm = { selectedTime ->
                    selectedTime?.let {
                        viewModel.onTimeSlotSelected(startTime, it)
                        showTimePickerStopDialog = false
                        startTime = null
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTimePicker(
    timePickerState: TimePickerState = rememberTimePickerState(),
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (selectedTimeStamp: LocalTime?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text("Cancel")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(state = timePickerState)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDatePicker(
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


@Composable
fun TimeSlotItem(timeSlot: TimeSlotUIItem, onRemove: (TimeSlotUIItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${timeSlot.startTime} ~ ${timeSlot.endTime}",
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        IconButton(onClick = { onRemove(timeSlot) }) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Time Slot")
        }
    }
}