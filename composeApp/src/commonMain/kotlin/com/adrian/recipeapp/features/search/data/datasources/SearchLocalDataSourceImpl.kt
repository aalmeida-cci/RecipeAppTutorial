package com.adrian.recipeapp.features.search.data.datasources

import com.adrian.recipeapp.features.common.data.database.daos.RecipeDao
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

class SearchLocalDataSourceImpl(private val recipeDao: RecipeDao) : SearchLocalDataSource {
    override suspend fun searchRecipes(query: String): List<RecipeItem> =
        recipeDao.searchRecipes(query)
}
