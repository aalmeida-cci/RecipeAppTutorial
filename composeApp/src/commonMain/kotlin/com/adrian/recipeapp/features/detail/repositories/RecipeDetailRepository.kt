package com.adrian.recipeapp.features.detail.repositories

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface RecipeDetailRepository {
    suspend fun getRecipesDetail(id: Long): Result<RecipeItem>
}