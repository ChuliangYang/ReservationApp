package com.example.reservationapp.ui.feature.availableProviders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.reservationapp.data.model.Provider
import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.ui.ProviderList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderListViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clientId: Int = savedStateHandle.toRoute<ProviderList>().clientId

    private val _uiState = MutableStateFlow<ProviderListUiState>(ProviderListUiState.Loading)
    val uiState: StateFlow<ProviderListUiState> = _uiState.asStateFlow()

    init {
        getProviderList()
    }

    private fun getProviderList() {
        viewModelScope.launch {
            runCatching {
                providerRepository.getAvailableProviderList(clientId)
            }.onSuccess { providerList ->
                _uiState.value = ProviderListUiState.Success(providerList)
            }.onFailure { throwable ->
                _uiState.value =
                    ProviderListUiState.Error(throwable.localizedMessage ?: "Unknown error")
            }
        }
    }
}

sealed interface ProviderListUiState {
    data object Loading : ProviderListUiState
    data class Success(val providerList: List<Provider>) : ProviderListUiState
    data class Error(val message: String) : ProviderListUiState
}