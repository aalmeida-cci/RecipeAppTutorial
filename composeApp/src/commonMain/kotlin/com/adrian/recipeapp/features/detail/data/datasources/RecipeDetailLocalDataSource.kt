package com.adrian.recipeapp.features.detail.data.datasources

import com.adrian.recipeapp.db.Recipe
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface RecipeDetailLocalDataSource {
    suspend fun getRecipeDetail(id: Long): RecipeItem?
    suspend fun saveRecipe(recipe: RecipeItem)
}