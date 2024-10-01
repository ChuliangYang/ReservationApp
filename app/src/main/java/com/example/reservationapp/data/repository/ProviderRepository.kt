package com.example.reservationapp.data.repository

import com.example.reservationapp.data.model.Provider
import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.data.model.Schedule
import com.example.reservationapp.data.model.TimeSlot
import java.time.LocalDate
import java.time.ZoneId

interface ProviderRepository {
    suspend fun getAvailableProviderList(clientId: Int): List<Provider>
    suspend fun addReservation(reservation: Reservation): Reservation?

    suspend fun getAvailableSlots(
        providerId: Int,
        length: ReserveBlockLength,
        zoneId: ZoneId,
        date: LocalDate
    ): List<TimeSlot>

    suspend fun confirmReservation(reservationId: Int): Reservation?
    suspend fun deleteReservation(reservationId: Int)
    suspend fun getReservation(providerId: Int, userId: Int): Reservation?
    suspend fun getSupportReservationLength(providerId: Int): List<ReserveBlockLength>
    suspend fun addSchedule(providerId: Int, scheduleList: List<Schedule>): List<Schedule>
    suspend fun getSchedule(providerId: Int): List<Schedule>
    suspend fun deleteSchedule(providerId: Int)
}