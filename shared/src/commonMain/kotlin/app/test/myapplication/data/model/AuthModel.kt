package app.test.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String? = null
)

@Serializable
data class AuthResponse(
    val id: Int? = null,
    val token: String? = null,
    val error: String? = null
)

data class RawApiResponse(
    val statusCode: Int,
    val statusMessage: String,
    val durationMs: Long,
    val headers: Map<String, List<String>>,
    val body: String,
    val isSuccess: Boolean
)
