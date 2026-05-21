package com.adrian.recipeapp.features.common.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeListApiResponse(
    @SerialName("meals")
    val meals: List<RecipeApiItem>
)
