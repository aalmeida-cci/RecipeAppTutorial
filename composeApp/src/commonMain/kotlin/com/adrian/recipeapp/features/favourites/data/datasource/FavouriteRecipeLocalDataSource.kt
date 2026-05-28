package com.adrian.recipeapp.features.favourites.data.datasource

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface FavouriteRecipeLocalDataSource {
    suspend fun getAllFavouriteRecipes(): List<RecipeItem>

    suspend fun addFavourite(recipeId: Long)

    suspend fun removeFavourite(recipeId: Long)
}
