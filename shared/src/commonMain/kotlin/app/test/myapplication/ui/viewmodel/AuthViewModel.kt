package app.test.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.test.myapplication.data.model.AuthRequest
import app.test.myapplication.data.model.AuthResponse
import app.test.myapplication.data.network.ReqResApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val apiClient: ReqResApiClient = ReqResApiClient()
) : ViewModel() {

    private val _email = MutableStateFlow("eve.holt@reqres.in")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("cityslicka")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isRegisterMode = MutableStateFlow(false)
    val isRegisterMode: StateFlow<Boolean> = _isRegisterMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _result = MutableStateFlow<AuthResponse?>(null)
    val result: StateFlow<AuthResponse?> = _result.asStateFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun toggleMode() {
        _isRegisterMode.value = !_isRegisterMode.value
        _result.value = null
    }

    fun setPreset(email: String, password: String) {
        _email.value = email
        _password.value = password
        _result.value = null
    }

    fun submit() {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = null
            try {
                val req = AuthRequest(
                    email = _email.value.trim(),
                    password = _password.value.ifBlank { null }
                )
                val resp = if (_isRegisterMode.value) {
                    apiClient.register(req)
                } else {
                    apiClient.login(req)
                }
                _result.value = resp
            } catch (e: Exception) {
                _result.value = AuthResponse(error = e.message ?: "Network error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
