package com.adrian.recipeapp.features.feed.data.datasource

import com.adrian.recipeapp.features.common.data.database.daos.RecipeDao
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

class FeedLocalDataSourceImpl(
    private val recipeDao: RecipeDao
): FeedLocalDataSource {
    override suspend fun getRecipesList(): List<RecipeItem> {
        return recipeDao.getAllRecipes()
    }

    override suspend fun saveRecipesList(recipes: List<RecipeItem>) {
        recipeDao.insertRecipesBulk(recipes)
    }
}