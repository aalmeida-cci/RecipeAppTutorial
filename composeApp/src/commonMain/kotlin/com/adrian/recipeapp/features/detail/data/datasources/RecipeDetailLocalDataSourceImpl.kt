package com.adrian.recipeapp.features.detail.data.datasources

import com.adrian.recipeapp.features.common.data.database.daos.RecipeDao
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

class RecipeDetailLocalDataSourceImpl(
    private val recipeDao: RecipeDao
): RecipeDetailLocalDataSource {


    override suspend fun getRecipeDetail(id: Long): RecipeItem? {
        return recipeDao.getRecipeById(id)
    }

    override suspend fun saveRecipe(recipe: RecipeItem) {
        recipeDao.insertRecipe(recipe)
    }
}