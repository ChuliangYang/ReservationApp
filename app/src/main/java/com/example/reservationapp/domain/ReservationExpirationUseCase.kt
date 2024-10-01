package com.example.reservationapp.domain

import com.example.reservationapp.core.ExpirationMonitor
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class ReservationExpirationUseCase @Inject constructor(
    private val expirationMonitor: ExpirationMonitor,
) {
    fun monitor(
        reservationId: Int,
        createAt: Instant,
        onExpired: (id: Int) -> Unit,
    ) {
        val now = Instant.now()
        val expiredAt = createAt.plusMillis(30.minutes.inWholeMilliseconds)

        if (now >= expiredAt) {
            expirationMonitor.stopMonitor(reservationId)
            onExpired(reservationId)
        } else {
            val remainingMillis = Duration.between(now, expiredAt).toMillis()
            expirationMonitor.monitor(
                reservationId,
                onExpired,
                remainingMillis
            )
        }
    }

    fun stopMonitor(reservationId: Int) {
        expirationMonitor.stopMonitor(reservationId)
    }
}