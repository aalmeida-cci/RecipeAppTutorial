package com.adrian.recipeapp.features.favourites.data.repositories

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface FavouriteRecipeRepository {
    suspend fun getALlFavouriteRecipe(): Result<List<RecipeItem>>

    suspend fun addFavourite(recipeId: Long)

    suspend fun removeFavourite(recipeId: Long)
}
