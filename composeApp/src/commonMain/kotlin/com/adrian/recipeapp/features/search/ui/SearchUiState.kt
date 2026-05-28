package com.adrian.recipeapp.features.search.ui

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

sealed interface SearchUiState {
    data object Idle : SearchUiState

    data object Loading : SearchUiState

    data class Success(val recipes: List<RecipeItem>) : SearchUiState

    data object Empty : SearchUiState

    data class Error(val throwable: Throwable) : SearchUiState
}
