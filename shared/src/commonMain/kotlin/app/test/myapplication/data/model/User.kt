package app.test.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val avatar: String
) {
    val fullName: String get() = "$firstName $lastName"

    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
}

@Serializable
data class Support(
    val url: String,
    val text: String
)

@Serializable
data class UserListResponse(
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int,
    val data: List<User>,
    val support: Support? = null
)

@Serializable
data class SingleUserResponse(
    val data: User,
    val support: Support? = null
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val job: String
)

@Serializable
data class CreateUserResponse(
    val id: String = "",
    val name: String,
    val job: String,
    val createdAt: String = ""
)

@Serializable
data class UpdateUserRequest(
    val name: String,
    val job: String
)

@Serializable
data class UpdateUserResponse(
    val name: String,
    val job: String,
    val updatedAt: String = ""
)
