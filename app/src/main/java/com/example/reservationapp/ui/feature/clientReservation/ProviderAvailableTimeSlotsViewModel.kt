package com.example.reservationapp.ui.feature.clientReservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.domain.ReserveTimeSlotUseCase
import com.example.reservationapp.ui.ProviderAvailableTimeSlots
import com.example.reservationapp.ui.fromBlockLengthRouteArgument
import com.example.reservationapp.ui.fromDateRouteArgument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProviderAvailableTimeSlotsViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val reserveTimeSlotUseCase: ReserveTimeSlotUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val blockLength: ReserveBlockLength =
        savedStateHandle.toRoute<ProviderAvailableTimeSlots>().length.fromBlockLengthRouteArgument()
    private val date: LocalDate =
        savedStateHandle.toRoute<ProviderAvailableTimeSlots>().date.fromDateRouteArgument()
    private val providerId: Int = savedStateHandle.toRoute<ProviderAvailableTimeSlots>().providerId
    private val _uiState =
        MutableStateFlow<ProviderAvailableTimeSlotsUIState>(ProviderAvailableTimeSlotsUIState.Loading)
    val uiState: StateFlow<ProviderAvailableTimeSlotsUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ProviderAvailableTimeSlotsEvent?>(null)
    val events: StateFlow<ProviderAvailableTimeSlotsEvent?> = _events.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        _uiState.value = ProviderAvailableTimeSlotsUIState.Loading
        viewModelScope.launch {
            runCatching {
                providerRepository.getAvailableSlots(
                    providerId,
                    blockLength,
                    ZoneId.systemDefault(),
                    date
                )
            }.onSuccess {
                _uiState.value = ProviderAvailableTimeSlotsUIState.ShowAvailableTimeSlots(it)
            }.onFailure(::emitErrorState)
        }
    }


    fun onTimeSlotSelected(timeSlot: TimeSlot) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProviderAvailableTimeSlotsUIState.ShowAvailableTimeSlots) {
                reserveTimeSlotUseCase.execute(providerId, date, timeSlot)
                    .onSuccess { reservation ->
                        reservation.id?.let {
                            _events.value =
                                ProviderAvailableTimeSlotsEvent.NavigateToReservation(
                                    it,
                                    reservation,
                                )
                        } ?: run(::emitWarningMessage)
                    }.onFailure(::emitWarningMessage)

            }
        }
    }

    private fun emitErrorState(throwable: Throwable? = null, message: String? = null) {
        _uiState.value = ProviderAvailableTimeSlotsUIState.Error(
            throwable?.localizedMessage ?: message ?: "Unknown error"
        )
    }

    private fun emitWarningMessage(throwable: Throwable? = null, message: String? = null) {
        _events.value = ProviderAvailableTimeSlotsEvent.Warning(
            throwable?.localizedMessage ?: message ?: "Unknown error"
        )
    }

    fun onEventHandled() {
        _events.value = null
    }
}

sealed interface ProviderAvailableTimeSlotsUIState {
    data object Loading : ProviderAvailableTimeSlotsUIState
    data class ShowAvailableTimeSlots(
        val availableTimeSlots: List<TimeSlot>,
    ) : ProviderAvailableTimeSlotsUIState

    data class Error(val message: String) : ProviderAvailableTimeSlotsUIState
}

sealed interface ProviderAvailableTimeSlotsEvent {
    data class Warning(val message: String) : ProviderAvailableTimeSlotsEvent
    data class NavigateToReservation(
        val reservationId: Int,
        val reservation: Reservation
    ) :
        ProviderAvailableTimeSlotsEvent
}