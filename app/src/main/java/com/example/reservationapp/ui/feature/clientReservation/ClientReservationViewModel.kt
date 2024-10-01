package com.example.reservationapp.ui.feature.clientReservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.domain.ReservationExpirationUseCase
import com.example.reservationapp.ui.ClientReservation
import com.example.reservationapp.ui.feature.providerSchedule.ScheduleUIItem
import com.example.reservationapp.ui.feature.providerSchedule.toUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ClientReservationViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val reservationExpirationUseCase: ReservationExpirationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val clientId: Int = savedStateHandle.toRoute<ClientReservation>().clientId
    private val providerId: Int = savedStateHandle.toRoute<ClientReservation>().providerId
    private val _uiState =
        MutableStateFlow<ClientReservationUIState>(ClientReservationUIState.Loading)
    val uiState: StateFlow<ClientReservationUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ClientReservationEventState?>(null)
    val events: StateFlow<ClientReservationEventState?> = _events.asStateFlow()

    init {
        refresh()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun refresh() {
        _uiState.value = ClientReservationUIState.Loading
        fetchReservation()
            .filter { it == null }
            .flatMapConcat {
                fetchSchedule()
            }.zip(fetchReservationBlockLength()) { schedule, blockLength ->
                ClientReservationUIState.ShowProviderSchedule(blockLength, schedule)
            }.onEach {
                _uiState.value = it
            }.catch {
                emitErrorState(it)
            }.launchIn(viewModelScope)

    }

    private fun fetchReservation(): Flow<Reservation?> {
        return flow {
            emit(providerRepository.getReservation(providerId, clientId))
        }.onEach {
            it?.let {
                _uiState.value = ClientReservationUIState.Reserved(it.toUIModel())
            }
        }
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

    fun onReservationConfirm(reservationId: Int) {
        reservationExpirationUseCase.stopMonitor(reservationId)
        viewModelScope.launch {
            runCatching {
                providerRepository.confirmReservation(reservationId)
            }.onSuccess {
                it?.let {
                    refresh()
                } ?: run(::emitWarningMessage)
            }.onFailure(::emitWarningMessage)
        }
    }

    fun onReservationDelete(reservationId: Int) {
        reservationExpirationUseCase.stopMonitor(reservationId)
        viewModelScope.launch {
            runCatching {
                providerRepository.deleteReservation(reservationId)
            }.onSuccess {
                refresh()
            }.onFailure(::emitWarningMessage)
        }
    }

    fun onRequestAvailableTimeSlots(date: LocalDate?, length: ReserveBlockLength) {
        if (date == null) {
            emitWarningMessage(message = "Please select a date first!")
            return
        }
        viewModelScope.launch {
            runCatching {
                providerRepository.getAvailableSlots(
                    providerId,
                    length,
                    ZoneId.systemDefault(),
                    date
                )
            }.onSuccess {
                _uiState.value = ClientReservationUIState.ShowAvailableTimeSlots(date, length, it)
            }.onFailure(::emitWarningMessage)
        }
    }

    fun onTimeSlotSelected(timeSlot: TimeSlot) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ClientReservationUIState.ShowAvailableTimeSlots) {
                val timeSlot = checkTimeSlotValidity(currentState.date, timeSlot)
                timeSlot?.let {
                    val reservation = Reservation(
                        id = null,
                        providerId = providerId,
                        date = currentState.date,
                        userId = clientId,
                        timeSlot = it,
                        createdAt = Instant.now(),
                        isConfirmed = false,
                    )

                    runCatching {
                        providerRepository.addReservation(reservation)
                    }.onSuccess { reservation ->
                        reservation?.let {
                            _uiState.value =
                                ClientReservationUIState.Reserved(reservation.toUIModel())
                            reservation.id?.let {
                                reservationExpirationUseCase.monitor(it, reservation.createdAt) {
                                    refresh()
                                }
                            }
                        } ?: run(::emitWarningMessage)
                    }.onFailure(::emitWarningMessage)
                }
            }
        }
    }

    private fun checkTimeSlotValidity(reservationDate: LocalDate, timeSlot: TimeSlot): TimeSlot? {
        val now = Instant.now()
        val reservationDateTime = reservationDate.atTime(timeSlot.startTime)
        val zoneId = ZoneId.systemDefault()
        val reservationStartTime = reservationDateTime.atZone(zoneId).toInstant()
        val duration = Duration.between(now, reservationStartTime)
        if (duration.isNegative || duration < Duration.ofHours(24)) {
            emitWarningMessage(message = "Reservation must be made at least 24 hours before!")
            return null
        }
        return timeSlot
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
