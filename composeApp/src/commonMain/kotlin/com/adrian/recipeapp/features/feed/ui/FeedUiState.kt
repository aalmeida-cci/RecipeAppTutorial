package com.adrian.recipeapp.features.feed.ui

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

data class FeedUiState (
    val recipesList: List<RecipeItem>? = null,
    val recipesListIsLoading: Boolean = true,
    val recipesListError: String? = null
)