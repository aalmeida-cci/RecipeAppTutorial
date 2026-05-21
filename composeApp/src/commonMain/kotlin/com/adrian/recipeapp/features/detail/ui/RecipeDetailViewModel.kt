package com.adrian.recipeapp.features.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adrian.recipeapp.features.detail.repositories.RecipeDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeDetailRepository: RecipeDetailRepository
): ViewModel() {

    private var _detailUiState = MutableStateFlow(RecipeDetailUiState())
    val detailUiState = _detailUiState.asStateFlow()

    suspend fun getRecipeDetail(id: Long) {
        viewModelScope.launch {
            val recipeDetailRes = recipeDetailRepository.getRecipesDetail(id)
            if (recipeDetailRes.isSuccess) {
                _detailUiState.value = detailUiState.value.copy(
                    recipeDetail = recipeDetailRes.getOrNull(),
                    recipesDetailIsLoading = false
                )
            } else {
                _detailUiState.value = detailUiState.value.copy(
                    recipesDetailError = recipeDetailRes.exceptionOrNull()?.message,
                    recipesDetailIsLoading = false
                )
            }
        }
    }
}