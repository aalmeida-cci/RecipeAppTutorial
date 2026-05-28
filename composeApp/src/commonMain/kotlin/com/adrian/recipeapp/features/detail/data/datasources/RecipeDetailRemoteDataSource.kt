package com.adrian.recipeapp.features.detail.data.datasources

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface RecipeDetailRemoteDataSource {
    suspend fun getRecipeDetail(id: Long): RecipeItem?
}
