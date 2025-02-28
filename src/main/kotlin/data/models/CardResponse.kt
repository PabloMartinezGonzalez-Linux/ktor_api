package data.models

import kotlinx.serialization.Serializable

@Serializable
data class CardResponse(
    val id: Int,
    val photo: String,
    val name: String,
    val description: String,
    val averageRating: Double,
    val hasImprovements: Boolean
)
