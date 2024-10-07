package com.example.reservationapp.ui.feature.clientReservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.domain.ReservationConfirmUseCase
import com.example.reservationapp.domain.ReservationDeleteUseCase
import com.example.reservationapp.domain.StartMonitoringReservationUseCase
import com.example.reservationapp.ui.ClientReservation
import com.example.reservationapp.ui.fromInstantRouteArgument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ClientReservationViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val reservationConfirmedUseCase: ReservationConfirmUseCase,
    private val reservationDeleteUseCase: ReservationDeleteUseCase,
    private val startMonitoringReservationUseCase: StartMonitoringReservationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val clientId: Int = savedStateHandle.toRoute<ClientReservation>().clientId
    private val providerId: Int = savedStateHandle.toRoute<ClientReservation>().providerId
    private val reservationId: Int? = savedStateHandle.toRoute<ClientReservation>().reservationId
    private val reservationCreationTime: Instant? =
        savedStateHandle.toRoute<ClientReservation>().createAt?.fromInstantRouteArgument()
    private val _uiState =
        MutableStateFlow<ClientReservationUIState>(ClientReservationUIState.Loading)
    val uiState: StateFlow<ClientReservationUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ClientReservationEventState?>(null)
    val events: StateFlow<ClientReservationEventState?> = _events.asStateFlow()

    init {
        handleNewCreateReservation(reservationId, reservationCreationTime)
        refresh()
    }

    private fun handleNewCreateReservation(
        reservationId: Int?,
        reservationCreationTime: Instant?
    ) {
        if (reservationId == null || reservationCreationTime == null) return
        viewModelScope.launch {
            startMonitoringReservationUseCase.execute(
                reservationId,
                reservationCreationTime
            ) {
                refresh()
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            runCatching {
                providerRepository.getReservation(providerId, clientId)
            }.onSuccess {
                it?.let {
                    _uiState.value = ClientReservationUIState.Reserved(it.toUIModel())
                } ?: run {
                    _events.value =
                        ClientReservationEventState.NavigateToProviderSchedule(providerId)
                }
            }.onFailure {
                emitErrorState(it)
            }
        }
    }

    fun onReservationConfirm(reservationId: Int) {
        viewModelScope.launch {
            reservationConfirmedUseCase.execute(reservationId)
                .onSuccess {
                    refresh()
                }.onFailure(::emitWarningMessage)
        }
    }

    fun onReservationDelete(reservationId: Int) {
        viewModelScope.launch {
            reservationDeleteUseCase.execute(reservationId)
                .onSuccess {
                    refresh()
                }.onFailure(::emitWarningMessage)
        }
    }

    private fun emitErrorState(throwable: Throwable? = null, message: String? = null) {
        _uiState.value = ClientReservationUIState.Error(
            throwable?.localizedMessage ?: message ?: "Unknown error"
        )
    }

    private fun emitWarningMessage(throwable: Throwable? = null, message: String? = null) {
        _events.value = ClientReservationEventState.Warning(
            throwable?.localizedMessage ?: message ?: "Unknown error"
        )
    }

    fun onEventHandled() {
        _events.value = null
    }
}

sealed interface ClientReservationUIState {
    data object Loading : ClientReservationUIState
    data class Reserved(val reservation: ReservationUI) : ClientReservationUIState
    data class Error(val message: String) : ClientReservationUIState
}

sealed interface ClientReservationEventState {
    data class Warning(val message: String) : ClientReservationEventState
    data class NavigateToProviderSchedule(val providerId: Int) : ClientReservationEventState

}

data class ReservationUI(
    val id: Int,
    val date: LocalDate,
    val timeSlot: TimeSlot,
    val isConfirmed: Boolean,
)

fun Reservation.toUIModel(): ReservationUI {
    return ReservationUI(
        id = id ?: -1,
        date = date,
        timeSlot = timeSlot,
        isConfirmed = isConfirmed,
    )
}
