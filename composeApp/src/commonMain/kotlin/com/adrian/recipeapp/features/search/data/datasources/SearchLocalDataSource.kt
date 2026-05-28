package com.adrian.recipeapp.features.search.data.datasources

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface SearchLocalDataSource {
    suspend fun searchRecipes(query: String): List<RecipeItem>
}
