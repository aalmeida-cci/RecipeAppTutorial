package com.adrian.recipeapp.features.favourites.ui

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

data class FavouriteScreenUiState (
    val itemsList: List<RecipeItem>? = null,
    val itemsIsLoading: Boolean = true,
    val itemListError: String? = null
)