package com.adrian.recipeapp.features.search.data.repositories

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem
import com.adrian.recipeapp.features.search.data.datasources.SearchLocalDataSource
import com.adrian.recipeapp.features.search.domain.repositories.SearchRepository

class SearchRepositoryImpl(
    private val localDataSource: SearchLocalDataSource
) : SearchRepository {
    override suspend fun searchRecipes(query: String): Result<List<RecipeItem>> = try {
        Result.success(localDataSource.searchRecipes(query))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
