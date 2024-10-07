package com.example.reservationapp.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


interface ExpirationMonitorInterface {
    fun monitor(
        id: Int,
        onExpired: (id: Int) -> Unit,
        expirationTimeMs: Long
    )

    fun stopMonitor(id: Int)
}

class ExpirationMonitor @Inject constructor() : ExpirationMonitorInterface {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val monitorCache = mutableMapOf<Int, Job>()

    override fun monitor(
        id: Int,
        onExpired: (id: Int) -> Unit,
        expirationTimeMs: Long
    ) {
        monitorCache[id] = coroutineScope.launch {
            delay(expirationTimeMs)
            monitorCache.remove(id)
            onExpired(id)
        }
    }

    override fun stopMonitor(id: Int) {
        monitorCache[id]?.cancel()
        monitorCache.remove(id)
    }
}

@Singleton
class ReservationExpirationMonitor @Inject constructor(
    private val expirationMonitor: ExpirationMonitor
) : ExpirationMonitorInterface by expirationMonitor