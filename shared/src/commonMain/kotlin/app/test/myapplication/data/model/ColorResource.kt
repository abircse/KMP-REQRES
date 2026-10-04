package app.test.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ColorResource(
    val id: Int,
    val name: String,
    val year: Int,
    val color: String,
    @SerialName("pantone_value") val pantoneValue: String
)

@Serializable
data class ResourceListResponse(
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int,
    val data: List<ColorResource>,
    val support: Support? = null
)
