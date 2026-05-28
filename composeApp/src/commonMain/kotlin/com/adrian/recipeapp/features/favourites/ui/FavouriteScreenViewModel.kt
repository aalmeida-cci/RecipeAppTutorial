package com.adrian.recipeapp.features.favourites.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.recipeapp.features.favourites.data.repositories.FavouriteRecipeRepository
import kotlin.collections.copy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavouriteScreenViewModel(
    private val favouriteRecipeRepository: FavouriteRecipeRepository
) : ViewModel() {
    private var _favouriteScreenUiState = MutableStateFlow(FavouriteScreenUiState())
    val favouriteScreenUiState = _favouriteScreenUiState.asStateFlow()

    init {
        viewModelScope.launch {
            getFavouriteRecipesList()
        }
    }

    suspend fun getFavouriteRecipesList() {
        val recipesList = favouriteRecipeRepository.getALlFavouriteRecipe()
        if (recipesList.isSuccess) {
            _favouriteScreenUiState.value =
                _favouriteScreenUiState.value.copy(
                    itemsList = recipesList.getOrDefault(emptyList()),
                    itemsIsLoading = false
                )
        } else {
            _favouriteScreenUiState.update {
                it.copy(
                    itemListError = recipesList.exceptionOrNull()?.message,
                    itemsIsLoading = false
                )
            }
        }
    }
}
