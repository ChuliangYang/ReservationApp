package com.example.reservationapp.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpirationMonitor @Inject constructor() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val monitorCache = mutableMapOf<Int, Job>()

    fun monitor(
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

    fun stopMonitor(id: Int) {
        monitorCache[id]?.cancel()
        monitorCache.remove(id)
    }
}