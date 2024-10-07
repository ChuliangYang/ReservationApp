package com.example.reservationapp.ui.feature.providerSchedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.core.ui.TimeSlotUIItem
import com.example.reservationapp.core.ui.toDataModel
import com.example.reservationapp.core.ui.toUIModel
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.domain.SubmitProviderScheduleUseCase
import com.example.reservationapp.ui.ProviderScheduling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProviderSchedulingViewModel @Inject constructor(
    private val submitProviderScheduleUseCase: SubmitProviderScheduleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val providerId: Int = savedStateHandle.toRoute<ProviderScheduling>().providerId

    private val _uiState =
        MutableStateFlow<ProviderSchedulingUIState>(ProviderSchedulingUIState.Scheduling())

    val uiState: StateFlow<ProviderSchedulingUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ProviderSchedulingEvent?>(null)
    val events: StateFlow<ProviderSchedulingEvent?> = _events.asStateFlow()


    // In the scheduling UI, each element is backed by a Flow because:
    // 1. It can be continuously updated and is produced asynchronously.
    // 2. Each Flow serves as the SSOT representing the original state.
    // 3. Each state flow should only update valid value that will be rendered to the UI.
    // 4. Once the new state is submitted to the state flow, the pipeline must auto render it to the UI accordingly.
    //
    // We avoid using state fields from the uiState because those fields are tailored for UI rendering convenience
    // and may lose some of the underlying details.
    private val startDateFlow = MutableStateFlow<LocalDate?>(null)

    private val endDateFlow = MutableStateFlow<LocalDate?>(null)

    private val timeSlotsFlow = MutableStateFlow<List<TimeSlot>>(emptyList())

    private val scheduleUpdate =
        combine(startDateFlow, endDateFlow, timeSlotsFlow) { startDate, endDate, timeSlots ->
            val currentState = _uiState.value
            if (currentState is ProviderSchedulingUIState.Scheduling) {
                _uiState.value = currentState.copy(
                    startDate = startDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    timeSlots = timeSlots.map { it.toUIModel() }
                )
            }
        }.launchIn(viewModelScope)

    fun onStartDateSelected(date: LocalDate) {
        val endDate = endDateFlow.value
        if (endDate == null || date.isEqual(endDate) || date.isBefore(endDate)) {
            startDateFlow.value = date
        } else {
            _events.value = ProviderSchedulingEvent.Warning("Please input valid start date!")
        }
    }

    fun onEndDateSelected(date: LocalDate) {
        val startDate = startDateFlow.value
        if (startDate == null || startDate.isEqual(date) || startDate.isBefore(date)) {
            endDateFlow.value = date
        } else {
            _events.value = ProviderSchedulingEvent.Warning("Please input valid end date!")
        }
    }

    fun onTimeSlotAdd(startTime: LocalTime?, endTime: LocalTime?) {
        if (startTime == null || endTime == null) {
            _events.value = ProviderSchedulingEvent.Warning("Please input valid start/end time!")
            return
        }
        if (startTime.isBefore(endTime)) {
            timeSlotsFlow.value += TimeSlot(startTime, endTime)
        } else {
            _events.value = ProviderSchedulingEvent.Warning("Please input valid start/end time!")
        }
    }

    fun removeTimeSlot(slot: TimeSlotUIItem) {
        timeSlotsFlow.value -= slot.toDataModel()
    }

    fun submitSchedule() {
        val currentState = _uiState.value
        if (currentState is ProviderSchedulingUIState.Scheduling) {
            val startDate = startDateFlow.value
            val endDate = endDateFlow.value
            val timeSlots = timeSlotsFlow.value
            if (startDate == null) {
                _events.value =
                    ProviderSchedulingEvent.Warning("Please input valid start date!")
                return
            }

            viewModelScope.launch {
                submitProviderScheduleUseCase.execute(
                    providerId,
                    startDate,
                    endDate,
                    timeSlots
                ).onSuccess {
                    _events.value = ProviderSchedulingEvent.NavigateToScheduleDetail(providerId)
                }.onFailure {
                    _events.value =
                        ProviderSchedulingEvent.Warning(it.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

    fun onEventHandled() {
        _events.value = null
    }
}

sealed interface ProviderSchedulingUIState {
    data class Scheduling(
        val startDate: String? = null,
        val endDate: String? = null,
        val timeSlots: List<TimeSlotUIItem> = emptyList(),
    ) : ProviderSchedulingUIState

    data class Error(val message: String) : ProviderSchedulingUIState
}

sealed interface ProviderSchedulingEvent {
    data class Warning(val message: String) : ProviderSchedulingEvent
    data class NavigateToScheduleDetail(val providerId: Int) : ProviderSchedulingEvent
}