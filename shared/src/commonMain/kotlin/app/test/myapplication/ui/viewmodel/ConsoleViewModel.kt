package app.test.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.test.myapplication.data.model.RawApiResponse
import app.test.myapplication.data.network.ReqResApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsoleViewModel(
    private val apiClient: ReqResApiClient = ReqResApiClient()
) : ViewModel() {

    private val _method = MutableStateFlow("GET")
    val method: StateFlow<String> = _method.asStateFlow()

    private val _endpoint = MutableStateFlow("users?page=2")
    val endpoint: StateFlow<String> = _endpoint.asStateFlow()

    private val _requestBody = MutableStateFlow("")
    val requestBody: StateFlow<String> = _requestBody.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _response = MutableStateFlow<RawApiResponse?>(null)
    val response: StateFlow<RawApiResponse?> = _response.asStateFlow()

    fun onMethodChanged(value: String) {
        _method.value = value
    }

    fun onEndpointChanged(value: String) {
        _endpoint.value = value
    }

    fun onRequestBodyChanged(value: String) {
        _requestBody.value = value
    }

    fun applyTemplate(method: String, endpoint: String, body: String = "") {
        _method.value = method
        _endpoint.value = endpoint
        _requestBody.value = body
        _response.value = null
    }

    fun execute() {
        viewModelScope.launch {
            _isLoading.value = true
            _response.value = null
            try {
                val res = apiClient.executeConsoleRequest(
                    method = _method.value,
                    endpointPath = _endpoint.value,
                    jsonBody = _requestBody.value.ifBlank { null }
                )
                _response.value = res
            } catch (e: Exception) {
                _response.value = RawApiResponse(
                    statusCode = 0,
                    statusMessage = "Exception",
                    durationMs = 0,
                    headers = emptyMap(),
                    body = e.message ?: "Failed to execute request",
                    isSuccess = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
