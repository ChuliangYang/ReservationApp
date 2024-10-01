package com.example.reservationapp.data.repository

import com.example.reservationapp.data.model.Provider
import com.example.reservationapp.data.model.Reservation
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.data.model.Schedule
import com.example.reservationapp.data.model.TimeSlot
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.random.Random

class ProviderRepositoryFakeImpl @Inject constructor() : ProviderRepository {

    private val availableProviders = listOf(
        Provider(1, "Provider 1"),
        Provider(2, "Provider 2"),
        Provider(3, "Provider 3")
    )

    private val supportReservationLength = listOf(
        ReserveBlockLength(15),
        ReserveBlockLength(30),
        ReserveBlockLength(60)
    )

    private val reservations = mutableMapOf<Int, Reservation>()
    private val schedules = mutableMapOf<Int, List<Schedule>>()

    override suspend fun getAvailableProviderList(clientId: Int): List<Provider> {
        return availableProviders
    }

    override suspend fun addReservation(reservation: Reservation): Reservation? {
        val reservation = reservation.copy(id = Random.nextInt())
        reservations[reservation.id!!] = reservation
        return reservation
    }

    override suspend fun getAvailableSlots(
        providerId: Int,
        length: ReserveBlockLength,
        zoneId: ZoneId,
        date: LocalDate
    ): List<TimeSlot> {
        return generateTimeSlots(length)
    }

    private fun generateTimeSlots(blockLength: ReserveBlockLength = ReserveBlockLength(60)): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()
        val startHour = 9
        val endHour = 17
        val blockDuration = blockLength.lengthInMinute

        var currentTime = LocalTime.of(startHour, 0)

        while (currentTime.isBefore(LocalTime.of(endHour, 0))) {
            val startTime = currentTime
            val endTime = startTime.plusMinutes(blockDuration.toLong())

            if (endTime.isAfter(LocalTime.of(endHour, 0))) {
                break
            }

            slots.add(TimeSlot(startTime, endTime))
            currentTime = endTime
        }

        return slots
    }

    override suspend fun confirmReservation(reservationId: Int): Reservation? {
        reservations[reservationId]?.copy(isConfirmed = true)?.let {
            reservations[reservationId] = it
            return it
        }
        return null
    }

    override suspend fun deleteReservation(reservationId: Int) {
        reservations.remove(reservationId)
    }

    override suspend fun getReservation(providerId: Int, userId: Int): Reservation? {
        return reservations.values.find { it.providerId == providerId && it.userId == userId }
    }

    override suspend fun getSupportReservationLength(providerId: Int): List<ReserveBlockLength> {
        return supportReservationLength
    }

    override suspend fun addSchedule(
        providerId: Int,
        scheduleList: List<Schedule>
    ): List<Schedule> {
        schedules[providerId] = schedules.getOrDefault(providerId, emptyList()) + scheduleList
        return schedules[providerId] ?: emptyList()
    }

    override suspend fun getSchedule(providerId: Int): List<Schedule> {
        if (providerId != 2) {
            return generateSchedulesForWeek(providerId, ZoneId.systemDefault())
        }
        return schedules[providerId] ?: emptyList()
    }

    private fun generateSchedulesForWeek(providerId: Int, zoneId: ZoneId): List<Schedule> {
        val today = LocalDate.now()
        val schedules = mutableListOf<Schedule>()
        val timeSlots = generateTimeSlots()

        // Loop through the next 7 days
        for (i in 0..6) {
            val currentDay = today.plusDays(i.toLong())
            val schedule = Schedule(
                id = i + 1,
                providerId = providerId,
                date = currentDay,
                zoneId = zoneId,
                timeSlots = timeSlots
            )
            schedules.add(schedule)
        }

        return schedules
    }

    override suspend fun deleteSchedule(providerId: Int) {
        schedules.remove(providerId)
    }
}