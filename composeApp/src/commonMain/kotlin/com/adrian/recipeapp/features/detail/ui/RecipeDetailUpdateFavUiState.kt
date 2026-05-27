package com.adrian.recipeapp.features.detail.ui

data class RecipeDetailUpdateFavUiState(
    val isSuccessResult: Boolean? = null,
    val isUpdatingFav: Boolean = false,
    val errorMsg: String? = null,
)