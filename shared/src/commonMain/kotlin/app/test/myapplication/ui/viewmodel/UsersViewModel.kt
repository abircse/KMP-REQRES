package app.test.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.test.myapplication.data.model.CreateUserResponse
import app.test.myapplication.data.model.UpdateUserResponse
import app.test.myapplication.data.model.User
import app.test.myapplication.data.network.ReqResApiClient
import app.test.myapplication.data.paging.UserPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UsersUiState {
    data object Loading : UsersUiState
    data class Success(
        val users: List<User>,
        val filteredUsers: List<User>,
        val page: Int,
        val totalPages: Int,
        val totalUsers: Int,
        val perPage: Int,
        val isInfiniteScroll: Boolean
    ) : UsersUiState
    data class Error(val message: String) : UsersUiState
}

data class UserActionFeedback(
    val title: String,
    val message: String,
    val isError: Boolean = false
)

class UsersViewModel(
    private val apiClient: ReqResApiClient = ReqResApiClient()
) : ViewModel() {

    val usersPagingFlow: Flow<PagingData<User>> = Pager(
        config = PagingConfig(pageSize = 3, enablePlaceholders = false)
    ) {
        UserPagingSource(apiClient = apiClient, perPage = 3)
    }.flow.cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _perPage = MutableStateFlow(3) // 3 items per page to showcase pagination clearly!
    val perPage: StateFlow<Int> = _perPage.asStateFlow()

    private val _isInfiniteScroll = MutableStateFlow(false)
    val isInfiniteScroll: StateFlow<Boolean> = _isInfiniteScroll.asStateFlow()

    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    private val _isCreateDialogOpen = MutableStateFlow(false)
    val isCreateDialogOpen: StateFlow<Boolean> = _isCreateDialogOpen.asStateFlow()

    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _feedback = MutableStateFlow<UserActionFeedback?>(null)
    val feedback: StateFlow<UserActionFeedback?> = _feedback.asStateFlow()

    private var loadedUsersList = mutableListOf<User>()
    private var totalPagesCount = 1
    private var totalUsersCount = 0

    init {
        loadUsers(page = 1)
    }

    fun loadUsers(page: Int = _currentPage.value) {
        _currentPage.value = page
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading
            try {
                val response = apiClient.getUsers(page, _perPage.value)
                loadedUsersList = response.data.toMutableList()
                totalPagesCount = response.totalPages
                totalUsersCount = response.total

                val filtered = filterUsers(loadedUsersList, _searchQuery.value)
                _uiState.value = UsersUiState.Success(
                    users = loadedUsersList,
                    filteredUsers = filtered,
                    page = response.page,
                    totalPages = response.totalPages,
                    totalUsers = response.total,
                    perPage = _perPage.value,
                    isInfiniteScroll = _isInfiniteScroll.value
                )
            } catch (e: Exception) {
                _uiState.value = UsersUiState.Error(
                    e.message ?: "Failed to load users from ReqRes API."
                )
            }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState !is UsersUiState.Success) return
        if (_isLoadingNextPage.value) return
        if (_currentPage.value >= totalPagesCount) return

        val nextPage = _currentPage.value + 1
        _currentPage.value = nextPage

        viewModelScope.launch {
            _isLoadingNextPage.value = true
            try {
                val response = apiClient.getUsers(nextPage, _perPage.value)
                loadedUsersList.addAll(response.data)

                val filtered = filterUsers(loadedUsersList, _searchQuery.value)
                _uiState.value = currentState.copy(
                    users = loadedUsersList.toList(),
                    filteredUsers = filtered,
                    page = nextPage,
                    totalPages = response.totalPages,
                    totalUsers = response.total
                )
            } catch (e: Exception) {
                _feedback.value = UserActionFeedback(
                    title = "Error Loading Page",
                    message = e.message ?: "Could not load page $nextPage",
                    isError = true
                )
            } finally {
                _isLoadingNextPage.value = false
            }
        }
    }

    fun toggleInfiniteScroll(enabled: Boolean) {
        _isInfiniteScroll.value = enabled
        loadUsers(1)
    }

    fun setPerPage(count: Int) {
        _perPage.value = count
        loadUsers(1)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val currentState = _uiState.value
        if (currentState is UsersUiState.Success) {
            _uiState.value = currentState.copy(
                filteredUsers = filterUsers(currentState.users, query)
            )
        }
    }

    private fun filterUsers(users: List<User>, query: String): List<User> {
        if (query.isBlank()) return users
        val q = query.trim().lowercase()
        return users.filter {
            it.firstName.lowercase().contains(q) ||
                    it.lastName.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.id.toString() == q
        }
    }

    fun selectUser(user: User?) {
        _selectedUser.value = user
    }

    fun showCreateDialog(show: Boolean) {
        _isCreateDialogOpen.value = show
    }

    fun showEditDialog(show: Boolean) {
        _isEditDialogOpen.value = show
    }

    fun createUser(name: String, job: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result: CreateUserResponse = apiClient.createUser(name, job)
                _isCreateDialogOpen.value = false
                _feedback.value = UserActionFeedback(
                    title = "User Created!",
                    message = "ID: ${result.id}\nName: ${result.name}\nJob: ${result.job}\nCreated At: ${result.createdAt}"
                )
            } catch (e: Exception) {
                _feedback.value = UserActionFeedback(
                    title = "Creation Failed",
                    message = e.message ?: "Error creating user",
                    isError = true
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun updateUser(id: Int, name: String, job: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result: UpdateUserResponse = apiClient.updateUser(id, name, job)
                _isEditDialogOpen.value = false
                _feedback.value = UserActionFeedback(
                    title = "User Updated!",
                    message = "Name: ${result.name}\nJob: ${result.job}\nUpdated At: ${result.updatedAt}"
                )
            } catch (e: Exception) {
                _feedback.value = UserActionFeedback(
                    title = "Update Failed",
                    message = e.message ?: "Error updating user",
                    isError = true
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val success = apiClient.deleteUser(id)
                _selectedUser.value = null
                if (success) {
                    _feedback.value = UserActionFeedback(
                        title = "User Deleted",
                        message = "User #$id successfully deleted from ReqRes."
                    )
                } else {
                    _feedback.value = UserActionFeedback(
                        title = "Deletion Failed",
                        message = "Server returned failure status for User #$id",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _feedback.value = UserActionFeedback(
                    title = "Error Deleting User",
                    message = e.message ?: "Could not delete user",
                    isError = true
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }
}
