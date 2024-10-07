package com.example.reservationapp.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservationapp.data.model.Schedule
import com.example.reservationapp.data.model.TimeSlot
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleList(scheduleList: List<ScheduleUIItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        scheduleList.forEach { section ->
            stickyHeader {
                SectionHeader(sectionTitle = section.date)
            }
            items(section.timeSlots) { item ->
                SectionItem(item)
            }
        }
    }
}

@Composable
fun SectionHeader(sectionTitle: String) {
    Text(
        text = sectionTitle,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    )
}

@Composable
fun SectionItem(item: TimeSlotUIItem) {
    ListItem(
        headlineContent = {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "${item.startTime} ~ ${item.endTime}",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
    )
}

data class ScheduleUIItem(
    val scheduleId: String,
    val date: String,
    val timeSlots: List<TimeSlotUIItem>
)

data class TimeSlotUIItem(
    val startTime: String,
    val endTime: String,
)

fun TimeSlot.toUIModel() = TimeSlotUIItem(
    startTime = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
    endTime = endTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
)

fun TimeSlotUIItem.toDataModel() = TimeSlot(
    startTime = LocalTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_TIME),
    endTime = LocalTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_TIME),
)

fun Schedule.toUIModel() = ScheduleUIItem(
    scheduleId = id.toString(),
    date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeSlots = timeSlots.map { it.toUIModel() }
)