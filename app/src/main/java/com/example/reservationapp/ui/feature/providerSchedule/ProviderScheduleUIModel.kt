package com.example.reservationapp.ui.feature.providerSchedule

import com.example.reservationapp.data.model.Schedule
import com.example.reservationapp.data.model.TimeSlot
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed interface ProviderUIState {
    data object Loading : ProviderUIState

    data class Scheduling(
        val startDate: String? = null,
        val endDate: String? = null,
        val timeSlots: List<TimeSlotUIItem> = emptyList(),
    ) : ProviderUIState

    data class Scheduled(val schedules: List<ScheduleUIItem>) : ProviderUIState
    data class Error(val message: String) : ProviderUIState
}

data class ScheduleUIItem(
    val scheduleId: String,
    val date: String,
    val timeSlots: List<TimeSlotUIItem>
)

data class TimeSlotUIItem(
    val startTime: String,
    val endTime: String,
)

sealed interface ProviderEventState {
    data class Warning(val message: String) : ProviderEventState
}

fun TimeSlot.toUIModel() = TimeSlotUIItem(
    startTime = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
    endTime = endTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
)

fun TimeSlotUIItem.toDataModel() = TimeSlot(
    startTime = LocalTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_TIME),
    endTime = LocalTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_TIME),
)

fun Schedule.toUIModel() = ScheduleUIItem(
    scheduleId = id.toString(),
    date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeSlots = timeSlots.map { it.toUIModel() }
)