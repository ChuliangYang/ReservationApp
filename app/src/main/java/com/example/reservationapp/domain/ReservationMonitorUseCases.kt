package com.example.reservationapp.domain

import com.example.reservationapp.core.util.ReservationExpirationMonitor
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class StartMonitoringReservationUseCase @Inject constructor(
    private val expirationMonitor: ReservationExpirationMonitor
) {
    suspend fun execute(
        reservationId: Int,
        createdAt: Instant = Instant.now(),
        onExpired: (id: Int) -> Unit
    ) {
        val expiredAt = createdAt.plusMillis(Duration.ofMinutes(30).toMillis())

        if (createdAt >= expiredAt) {
            expirationMonitor.stopMonitor(reservationId)
            onExpired(reservationId)
        } else {
            val remainingMillis = Duration.between(createdAt, expiredAt).toMillis()
            expirationMonitor.monitor(
                reservationId,
                onExpired,
                remainingMillis
            )
        }
    }
}

class StopMonitoringReservationUseCase @Inject constructor(
    private val expirationMonitor: ReservationExpirationMonitor
) {
    suspend fun execute(reservationId: Int) {
        expirationMonitor.stopMonitor(reservationId)
    }
}