package app.test.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.test.myapplication.data.model.ColorResource
import app.test.myapplication.data.network.ReqResApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ResourcesUiState {
    data object Loading : ResourcesUiState
    data class Success(
        val resources: List<ColorResource>,
        val page: Int,
        val totalPages: Int,
        val total: Int
    ) : ResourcesUiState
    data class Error(val message: String) : ResourcesUiState
}

class ResourcesViewModel(
    private val apiClient: ReqResApiClient = ReqResApiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResourcesUiState>(ResourcesUiState.Loading)
    val uiState: StateFlow<ResourcesUiState> = _uiState.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    init {
        loadResources(1)
    }

    fun loadResources(page: Int = _currentPage.value) {
        _currentPage.value = page
        viewModelScope.launch {
            _uiState.value = ResourcesUiState.Loading
            try {
                val response = apiClient.getResources(page)
                _uiState.value = ResourcesUiState.Success(
                    resources = response.data,
                    page = response.page,
                    totalPages = response.totalPages,
                    total = response.total
                )
            } catch (e: Exception) {
                _uiState.value = ResourcesUiState.Error(
                    e.message ?: "Failed to load color resources"
                )
            }
        }
    }
}
