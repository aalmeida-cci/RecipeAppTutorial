package com.adrian.recipeapp.features.search.domain.repositories

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface SearchRepository {
    suspend fun searchRecipes(query: String): Result<List<RecipeItem>>
}
