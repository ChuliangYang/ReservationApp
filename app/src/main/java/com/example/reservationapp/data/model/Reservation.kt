package com.example.reservationapp.data.model

import java.time.Instant
import java.time.LocalDate

data class Reservation(
    val id: Int?,
    val userId: Int,
    val providerId: Int,
    val date: LocalDate,
    val timeSlot: TimeSlot,
    val isConfirmed: Boolean,
    val createdAt: Instant,
)

