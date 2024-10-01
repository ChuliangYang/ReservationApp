package com.example.reservationapp.data.model

import java.time.LocalDate
import java.time.ZoneId

data class Schedule(
    val id: Int?,
    val providerId: Int,
    val date: LocalDate,
    val zoneId: ZoneId,
    val timeSlots: List<TimeSlot>
)