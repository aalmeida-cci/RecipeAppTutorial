package com.adrian.recipeapp.features.detail.data.datasources

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface RecipeDetailLocalDataSource {
    suspend fun getRecipeDetail(id: Long): RecipeItem?

    suspend fun saveRecipe(recipe: RecipeItem)

    suspend fun addFavourite(id: Long)

    suspend fun removeFavourite(recipeId: Long)

    suspend fun isFavourite(recipeId: Long): Boolean
}
