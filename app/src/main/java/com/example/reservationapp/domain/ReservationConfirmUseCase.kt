package com.example.reservationapp.domain

import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.repository.ProviderRepository
import javax.inject.Inject

class ReservationConfirmUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val stopMonitoringReservationUseCase: StopMonitoringReservationUseCase
) {
    suspend fun execute(reservationId: Int): Result<Reservation> {
        return runCatching {
            val reservation = providerRepository.confirmReservation(reservationId)
            reservation?.id?.let {
                stopMonitoringReservationUseCase.execute(reservationId)
                return@runCatching reservation
            }
            throw IllegalStateException("Reservation not found")
        }
    }
}