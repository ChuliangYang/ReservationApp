package com.example.reservationapp.domain

import com.example.reservationapp.data.model.Schedule
import com.example.reservationapp.data.model.TimeSlot
import com.example.reservationapp.data.repository.ProviderRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class SubmitProviderScheduleUseCase @Inject constructor(
    private val providerRepository: ProviderRepository,
) {
    suspend fun execute(
        providerId: Int,
        startDate: LocalDate,
        endDate: LocalDate?,
        timeSlots: List<TimeSlot>
    ): Result<List<Schedule>> {
        if (startDate.isAfter(endDate)) {
            return Result.failure(IllegalArgumentException("Start date cannot be after end date"))
        }

        if (timeSlots.isEmpty()) {
            return Result.failure(IllegalArgumentException("Time slots cannot be empty"))
        }

        val dates = generateDateRange(startDate, endDate ?: startDate)
        val scheduleList = dates.map {
            Schedule(
                id = null, // BE generate filed
                providerId = providerId,
                date = it,
                zoneId = ZoneId.systemDefault(),
                timeSlots = timeSlots
            )
        }
        return runCatching {
            providerRepository.addSchedule(providerId, scheduleList)
        }
    }

    private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return dates
    }
}