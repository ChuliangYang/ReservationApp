package com.example.reservationapp.domain

import com.example.reservationapp.data.model.Client
import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.data.repository.UserRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ReserveTimeSlotUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val userRepository: UserRepository,
) {
    suspend fun execute(
        providerId: Int,
        reservationDate: LocalDate,
        timeSlot: TimeSlot,
    ): Result<Reservation> {
        return runCatching {
            val client = userRepository.getCurrentUser() as? Client
                ?: throw IllegalStateException("Client not found!")

            checkTimeSlotValidity(reservationDate, timeSlot)

            val reservation = Reservation(
                id = null,
                providerId = providerId,
                date = reservationDate,
                userId = client.id,
                timeSlot = timeSlot,
                createdAt = Instant.now(),
                isConfirmed = false,
            )
            providerRepository.addReservation(reservation)
                ?: throw IllegalStateException("Fail to add reservation!")
        }
    }

    private fun checkTimeSlotValidity(reservationDate: LocalDate, timeSlot: TimeSlot) {
        val now = Instant.now()
        val reservationDateTime = reservationDate.atTime(timeSlot.startTime)
        val zoneId = ZoneId.systemDefault()
        val reservationStartTime = reservationDateTime.atZone(zoneId).toInstant()
        val duration = Duration.between(now, reservationStartTime)
        if (duration.isNegative || duration < Duration.ofHours(24)) {
            throw IllegalArgumentException("Reservation must be made at least 24 hours before!")
        }
    }
}