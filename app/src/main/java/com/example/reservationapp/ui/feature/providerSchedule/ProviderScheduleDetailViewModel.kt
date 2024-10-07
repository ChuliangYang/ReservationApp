package com.example.reservationapp.ui.feature.providerSchedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.core.ui.ScheduleUIItem
import com.example.reservationapp.core.ui.toUIModel
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.ui.ProviderScheduleDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderScheduleDetailViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val providerId: Int = savedStateHandle.toRoute<ProviderScheduleDetail>().providerId

    private val _uiState = MutableStateFlow<ProviderUIState>(ProviderUIState.Loading)

    val uiState: StateFlow<ProviderUIState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ProviderEventState?>(null)
    val events: StateFlow<ProviderEventState?> = _events.asStateFlow()

    init {
        fetchSchedules()
    }

    fun deleteSchedule() {
        viewModelScope.launch {
            runCatching {
                providerRepository.deleteSchedule(providerId)
            }.onSuccess {
                _events.value = ProviderEventState.NavigateToScheduling(providerId)
            }.onFailure {
                _events.value =
                    ProviderEventState.Warning(it.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun onEventHandled() {
        _events.value = null
    }

    private fun fetchSchedules() {
        viewModelScope.launch {
            val result = runCatching {
                providerRepository.getSchedule(providerId)
            }

            result.onSuccess { schedule ->
                if (schedule.isEmpty()) {
                    _events.value = ProviderEventState.NavigateToScheduling(providerId)
                } else {
                    val scheduleUIItems = schedule.map {
                        it.toUIModel()
                    }
                    _uiState.value = ProviderUIState.Scheduled(scheduleUIItems)
                }
            }.onFailure { throwable ->
                _uiState.value = ProviderUIState.Error(
                    throwable.message ?: "An unknown error occurred"
                )
            }
        }
    }
}

sealed interface ProviderUIState {
    data object Loading : ProviderUIState
    data class Scheduled(val schedules: List<ScheduleUIItem>) : ProviderUIState
    data class Error(val message: String) : ProviderUIState
}

sealed interface ProviderEventState {
    data class Warning(val message: String) : ProviderEventState
    data class NavigateToScheduling(val providerId: Int) : ProviderEventState
}