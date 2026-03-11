package com.noodrop.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.repository.NoodropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String?     = null,
    val mode: AuthMode     = AuthMode.LOGIN,
)

enum class AuthMode { LOGIN, SIGNUP }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun toggleMode() = _state.update {
        it.copy(mode = if (it.mode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN, error = null)
    }

    fun submit(email: String, password: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                if (_state.value.mode == AuthMode.LOGIN) repo.signIn(email, password)
                else repo.signUp(email, password)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
