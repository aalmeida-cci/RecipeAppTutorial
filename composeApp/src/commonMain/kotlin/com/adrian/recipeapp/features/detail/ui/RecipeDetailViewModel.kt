package com.adrian.recipeapp.features.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.recipeapp.features.detail.repositories.RecipeDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeDetailRepository: RecipeDetailRepository
) : ViewModel() {
    private var _detailUiState = MutableStateFlow(RecipeDetailUiState())
    val detailUiState = _detailUiState.asStateFlow()

    private var _updateFavUiState = MutableStateFlow(RecipeDetailUpdateFavUiState())
    val updateFavUiState = _updateFavUiState.asStateFlow()

    suspend fun getRecipeDetail(id: Long) {
        viewModelScope.launch {
            val recipeDetailRes = recipeDetailRepository.getRecipesDetail(id)
            if (recipeDetailRes.isSuccess) {
                _detailUiState.value =
                    detailUiState.value.copy(
                        recipeDetail = recipeDetailRes.getOrNull(),
                        recipesDetailIsLoading = false
                    )
            } else {
                _detailUiState.value =
                    detailUiState.value.copy(
                        recipesDetailError = recipeDetailRes.exceptionOrNull()?.message,
                        recipesDetailIsLoading = false
                    )
            }
        }
    }

    fun updateFavourite(recipeId: Long, isAddFavourite: Boolean) {
        viewModelScope.launch {
            try {
                _updateFavUiState.value =
                    _updateFavUiState.value.copy(
                        isUpdatingFav = true
                    )

                if (isAddFavourite) {
                    recipeDetailRepository.addFavourite(recipeId)
                } else {
                    recipeDetailRepository.removeFavourite(recipeId)
                }

                // refresh details
                _detailUiState.value =
                    _detailUiState.value.copy(
                        recipeDetail =
                        _detailUiState.value.recipeDetail?.copy(
                            isFavorite = isAddFavourite
                        )
                    )
                _updateFavUiState.value =
                    _updateFavUiState.value.copy(
                        isSuccessResult = true,
                        isUpdatingFav = false
                    )
            } catch (e: Exception) {
                println("updateFavourite failed: ${e.message}")
                _updateFavUiState.value =
                    _updateFavUiState.value.copy(
                        errorMsg = e.message,
                        isUpdatingFav = false
                    )
            }
        }
    }
}
