package com.example.reservationapp.ui.feature.providerSchedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.data.model.Schedule
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.ui.ProviderSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProviderScheduleViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val providerId: Int = savedStateHandle.toRoute<ProviderSchedule>().providerId

    private val _uiState = MutableStateFlow<ProviderUIState>(ProviderUIState.Loading)

    val uiState: StateFlow<ProviderUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ProviderEventState?>(null)
    val events: StateFlow<ProviderEventState?> = _events.asStateFlow()

    private val startDateFlow = MutableStateFlow<LocalDate?>(null).apply {
        onEach {
            handleNewStartDate(it)
        }
            .launchIn(viewModelScope)
    }

    private val endDateFlow = MutableStateFlow<LocalDate?>(null).apply {
        onEach {
            handleNewEndDate(it)
        }
            .launchIn(viewModelScope)
    }

    private val timeSlotsFlow = MutableStateFlow<List<TimeSlot>>(emptyList()).apply {
        this.onEach {
            handleNewTimeSlotList(it)
        }.launchIn(viewModelScope)
    }

    private val timeSlotToAdd = MutableSharedFlow<TimeSlot>().apply {
        this.onEach {
            handleNewTimeSlot(it)
        }.launchIn(viewModelScope)
    }

    init {
        observeUIStateReset()
        fetchSchedules()
    }

    fun onStartDateSelected(date: LocalDate) {
        startDateFlow.value = date
    }

    fun onEndDateSelected(date: LocalDate) {
        endDateFlow.value = date
    }

    fun onTimeSlotSelected(startTime: LocalTime?, endTime: LocalTime?) {
        if (startTime == null || endTime == null) {
            _events.value = ProviderEventState.Warning("Please input valid start/end time!")
            return
        }
        viewModelScope.launch {
            timeSlotToAdd.emit(TimeSlot(startTime, endTime))
        }
    }

    fun removeTimeSlot(slot: TimeSlotUIItem) {
        timeSlotsFlow.value -= slot.toDataModel()
    }

    fun submitSchedule() {
        viewModelScope.launch {

            val currentState = _uiState.value
            if (currentState is ProviderUIState.Scheduling) {
                val startDate = startDateFlow.value
                val endDate = endDateFlow.value
                val timeSlots = timeSlotsFlow.value
                if (startDate == null || timeSlots.isEmpty()) {
                    _events.value = ProviderEventState.Warning("Please input valid schedule!")
                    return@launch
                }

                val dates = generateDateRange(startDate, endDate ?: startDate)
                val scheduleList = dates.map {
                    Schedule(
                        id = null, // BE generate filed
                        providerId = providerId,
                        date = it,
                        zoneId = ZoneId.systemDefault(),
                        timeSlots = timeSlots
                    )
                }
                val result = runCatching {
                    providerRepository.addSchedule(providerId, scheduleList)
                }

                result.onSuccess { schedules ->
                    _uiState.value = ProviderUIState.Scheduled(schedules.map { it.toUIModel() })
                }.onFailure {
                    _events.value =
                        ProviderEventState.Warning(it.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

    fun deleteSchedule() {
        viewModelScope.launch {
            runCatching {
                providerRepository.deleteSchedule(providerId)
            }.onSuccess {
                _uiState.value = ProviderUIState.Scheduling()
            }.onFailure {
                _events.value =
                    ProviderEventState.Warning(it.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun onEventHandled() {
        _events.value = null
    }

    private fun observeUIStateReset() {
        _uiState.onEach {
            if (it !is ProviderUIState.Scheduling) {
                resetSchedulingState()
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchSchedules() {
        viewModelScope.launch {
            val result = runCatching {
                providerRepository.getSchedule(providerId)
            }

            result.onSuccess { schedule ->
                if (schedule.isEmpty()) {
                    _uiState.value = ProviderUIState.Scheduling()
                } else {
                    // Map schedules to UI items
                    val scheduleUIItems = schedule.map {
                        it.toUIModel()
                    }
                    _uiState.value = ProviderUIState.Scheduled(scheduleUIItems)
                }
            }.onFailure { throwable ->
                _uiState.value = ProviderUIState.Error(
                    throwable.message ?: "An unknown error occurred"
                )
            }
        }
    }

    private fun handleNewStartDate(date: LocalDate?) {
        val currentState = _uiState.value

        if (currentState !is ProviderUIState.Scheduling) {
            return
        }

        if (date == null) {
            _uiState.value = currentState.copy(
                startDate = null,
            )
            return
        }

        val endDate = endDateFlow.value

        if (endDate == null || date.isEqual(endDate) || date.isBefore(endDate)) {
            _uiState.value = currentState.copy(
                startDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            )
        } else {
            startDateFlow.value = null
            _events.value = ProviderEventState.Warning("Please input valid start date!")
        }
    }

    private fun handleNewEndDate(date: LocalDate?) {

        val currentState = _uiState.value

        if (currentState !is ProviderUIState.Scheduling) {
            return
        }

        if (date == null) {
            _uiState.value = currentState.copy(
                endDate = null,
            )
            return
        }

        val startDate = startDateFlow.value
        if (startDate == null || startDate.isEqual(date) || startDate.isBefore(date)) {
            _uiState.value = currentState.copy(
                endDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            )
        } else {
            endDateFlow.value = null
            _events.value = ProviderEventState.Warning("Please input valid end date!")
        }
    }

    private fun handleNewTimeSlot(timeSlot: TimeSlot) {
        val startTime = timeSlot.startTime
        val endTime = timeSlot.endTime
        if (startTime.isBefore(endTime)) {
            timeSlotsFlow.value += timeSlot
        } else {
            _events.value = ProviderEventState.Warning("Please input valid start/end time!")
        }
    }

    private fun handleNewTimeSlotList(timeSlots: List<TimeSlot>) {
        val currentState = _uiState.value
        if (currentState is ProviderUIState.Scheduling) {
            _uiState.value = currentState.copy(
                timeSlots = timeSlots.map { it.toUIModel() }
            )
        }
    }

    private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return dates
    }

    private fun resetSchedulingState() {
        startDateFlow.value = null
        endDateFlow.value = null
        timeSlotsFlow.value = emptyList()
    }
}