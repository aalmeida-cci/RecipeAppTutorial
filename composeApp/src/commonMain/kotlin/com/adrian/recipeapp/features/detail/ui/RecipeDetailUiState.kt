package com.adrian.recipeapp.features.detail.ui

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

data class RecipeDetailUiState(
    val recipeDetail: RecipeItem? = null,
    val recipesDetailIsLoading: Boolean = true,
    val recipesDetailError: String? = null
)
