package com.example.reservationapp.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservationapp.data.model.UserType
import com.example.reservationapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _event = MutableStateFlow<LoginEventState?>(null)
    val event: StateFlow<LoginEventState?> = _event.asStateFlow()

    fun login(account: String, password: String, userType: UserType) {
        viewModelScope.launch {
            val result = runCatching {
                userRepository.login(account, password, userType)
            }

            result.onSuccess { user ->
                when (userType) {
                    UserType.PROVIDER -> {
                        _event.value = LoginEventState.NavigateToProvider(user.id)
                    }

                    UserType.CLIENT -> {
                        _event.value = LoginEventState.NavigateToClient(user.id)
                    }
                }
            }.onFailure { throwable ->
                _event.value =
                    LoginEventState.DisplayErrorMessage(
                        throwable.message ?: "Unknown Error"
                    )
            }
        }
    }

    fun onEventHandled() {
        _event.value = null
    }
}

sealed interface LoginEventState {
    data class NavigateToProvider(val providerId: Int) : LoginEventState
    data class NavigateToClient(val clientId: Int) : LoginEventState
    data class DisplayErrorMessage(val message: String) : LoginEventState
}
