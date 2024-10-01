package com.example.reservationapp.ui.feature.clientReservation

import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.ui.feature.providerSchedule.ScheduleUIItem
import java.time.LocalDate


sealed interface ClientReservationUIState {
    data object Loading : ClientReservationUIState

    data class ShowProviderSchedule(
        val reserveBlockLength: List<ReserveBlockLength>,
        val schedule: List<ScheduleUIItem>,
    ) : ClientReservationUIState

    data class ShowAvailableTimeSlots(
        val date: LocalDate,
        val length: ReserveBlockLength,
        val availableTimeSlots: List<TimeSlot>,
    ) : ClientReservationUIState

    data class Reserved(val reservation: ReservationUI) : ClientReservationUIState
    data class Error(val message: String) : ClientReservationUIState
}

sealed interface ClientReservationEventState {
    data class Warning(val message: String) : ClientReservationEventState
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