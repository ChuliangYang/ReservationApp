package com.example.reservationapp.ui.feature.clientReservation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reservationapp.data.model.Reservation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderAvailableTimeSlotsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReservation: (reservationId: Int, Reservation) -> Unit,
    viewModel: ProviderAvailableTimeSlotsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.onEach {
            it?.let { event ->
                when (event) {
                    is ProviderAvailableTimeSlotsEvent.Warning -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }

                    is ProviderAvailableTimeSlotsEvent.NavigateToReservation -> {
                        onNavigateToReservation(event.reservationId, event.reservation)
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
                    Text("Available Time Slots")
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
    ) { paddingValues ->
        when (val uiState = uiState) {
            is ProviderAvailableTimeSlotsUIState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProviderAvailableTimeSlotsUIState.Error -> {
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

            is ProviderAvailableTimeSlotsUIState.ShowAvailableTimeSlots -> {
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
        }
    }
}

