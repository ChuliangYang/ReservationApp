package com.example.reservationapp.ui.feature.clientReservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.core.ui.ScheduleUIItem
import com.example.reservationapp.core.ui.toUIModel
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.ui.ClientReservation
import com.example.reservationapp.ui.ClientSideProviderSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ClientSideProviderScheduleViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val providerId: Int = savedStateHandle.toRoute<ClientSideProviderSchedule>().providerId
    private val _uiState =
        MutableStateFlow<ClientSideProviderScheduleUIState>(ClientSideProviderScheduleUIState.Loading)
    val uiState: StateFlow<ClientSideProviderScheduleUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ClientSideProviderScheduleEvent?>(null)
    val events: StateFlow<ClientSideProviderScheduleEvent?> = _events.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        _uiState.value = ClientSideProviderScheduleUIState.Loading
        fetchSchedule().zip(fetchReservationBlockLength()) { schedule, blockLength ->
            ClientSideProviderScheduleUIState.ShowProviderSchedule(blockLength, schedule)
        }.onEach {
            _uiState.value = it
        }.catch {
            emitErrorState(it)
        }.launchIn(viewModelScope)
    }

    private fun fetchSchedule(): Flow<List<ScheduleUIItem>> {
        return flow {
            emit(providerRepository.getSchedule(providerId))
        }.map {
            it.map { it.toUIModel() }
        }
    }

    private fun fetchReservationBlockLength(): Flow<List<ReserveBlockLength>> {
        return flow {
            emit(providerRepository.getSupportReservationLength(providerId))
        }
    }

    fun onRequestAvailableTimeSlots(date: LocalDate?, length: ReserveBlockLength) {
        if (date == null) {
            emitWarningMessage(message = "Please select a date first!")
            return
        }
        _events.value = ClientSideProviderScheduleEvent.NavigateToAvailableTimeSlot(
            providerId,
            date,
            length
        )
    }

    private fun emitErrorState(throwable: Throwable? = null, message: String? = null) {
        _uiState.value = ClientSideProviderScheduleUIState.Error(
            throwable?.localizedMessage ?: message ?: "Unknown error"
        )
    }

    private fun emitWarningMessage(throwable: Throwable? = null, message: String? = null) {
        _events.value = ClientSideProviderScheduleEvent.Warning(
            throwable?.localizedMessage ?: message ?: "Unknown error"
        )
    }

    fun onEventHandled() {
        _events.value = null
    }
}

sealed interface ClientSideProviderScheduleUIState {
    data object Loading : ClientSideProviderScheduleUIState

    data class ShowProviderSchedule(
        val reserveBlockLength: List<ReserveBlockLength>,
        val schedule: List<ScheduleUIItem>,
    ) : ClientSideProviderScheduleUIState

    data class Error(val message: String) : ClientSideProviderScheduleUIState
}

sealed interface ClientSideProviderScheduleEvent {
    data class Warning(val message: String) : ClientSideProviderScheduleEvent
    data class NavigateToAvailableTimeSlot(
        val providerId: Int,
        val date: LocalDate,
        val length: ReserveBlockLength,
    ) : ClientSideProviderScheduleEvent
}
