package com.example.reservationapp.domain

import com.example.reservationapp.data.repository.ProviderRepository
import javax.inject.Inject

class ReservationDeleteUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val stopMonitoringReservationUseCase: StopMonitoringReservationUseCase
) {
    suspend fun execute(reservationId: Int): Result<Unit> {
        return runCatching {
            providerRepository.deleteReservation(reservationId)
            stopMonitoringReservationUseCase.execute(reservationId)
        }
    }
}