package app.test.myapplication.data.network

import app.test.myapplication.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.TimeSource

class ReqResApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = true
        isLenient = true
    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val baseUrl = "https://reqres.in/api"

    suspend fun getUsers(page: Int = 1, perPage: Int = 6): UserListResponse {
        return httpClient.get("$baseUrl/users") {
            parameter("page", page)
            parameter("per_page", perPage)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header("x-app-reqres", "android-compose-kmp")
        }.body()
    }

    suspend fun getUser(id: Int): User {
        val response: SingleUserResponse = httpClient.get("$baseUrl/users/$id") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }.body()
        return response.data
    }

    suspend fun createUser(name: String, job: String): CreateUserResponse {
        return httpClient.post("$baseUrl/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = name, job = job))
        }.body()
    }

    suspend fun updateUser(id: Int, name: String, job: String): UpdateUserResponse {
        return httpClient.put("$baseUrl/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(name = name, job = job))
        }.body()
    }

    suspend fun deleteUser(id: Int): Boolean {
        val response = httpClient.delete("$baseUrl/users/$id")
        return response.status.isSuccess()
    }

    suspend fun getResources(page: Int = 1, perPage: Int = 6): ResourceListResponse {
        return httpClient.get("$baseUrl/unknown") {
            parameter("page", page)
            parameter("per_page", perPage)
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }.body()
    }

    suspend fun login(authRequest: AuthRequest): AuthResponse {
        val response = httpClient.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            try {
                response.body<AuthResponse>()
            } catch (e: Exception) {
                AuthResponse(error = "Request failed with code ${response.status.value}")
            }
        }
    }

    suspend fun register(authRequest: AuthRequest): AuthResponse {
        val response = httpClient.post("$baseUrl/register") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            try {
                response.body<AuthResponse>()
            } catch (e: Exception) {
                AuthResponse(error = "Request failed with code ${response.status.value}")
            }
        }
    }

    suspend fun executeConsoleRequest(
        method: String,
        endpointPath: String,
        jsonBody: String?
    ): RawApiResponse {
        val mark = TimeSource.Monotonic.markNow()
        val fullUrl = if (endpointPath.startsWith("http")) endpointPath else "$baseUrl/${endpointPath.removePrefix("/")}"
        val httpMethod = HttpMethod.parse(method.uppercase())

        val response = httpClient.request(fullUrl) {
            this.method = httpMethod
            header(HttpHeaders.Accept, ContentType.Application.Json)
            if (!jsonBody.isNull0orBlank() && (httpMethod == HttpMethod.Post || httpMethod == HttpMethod.Put || httpMethod == HttpMethod.Patch)) {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
        }

        val duration = mark.elapsedNow().inWholeMilliseconds
        val bodyText = response.bodyAsText()

        val headersMap = mutableMapOf<String, List<String>>()
        response.headers.forEach { key, values ->
            headersMap[key] = values
        }

        return RawApiResponse(
            statusCode = response.status.value,
            statusMessage = response.status.description,
            durationMs = duration,
            headers = headersMap,
            body = bodyText,
            isSuccess = response.status.isSuccess()
        )
    }

    private fun String?.isNull0orBlank(): Boolean = this == null || this.trim().isEmpty()
}
