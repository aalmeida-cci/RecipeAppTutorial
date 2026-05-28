package com.adrian.recipeapp.features.favourites.data.repositories

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem
import com.adrian.recipeapp.features.favourites.data.datasource.FavouriteRecipeLocalDataSource

class FavouriteRecipeRepositoryImpl(
    private val favouriteRecipeLocalDataSource: FavouriteRecipeLocalDataSource
) : FavouriteRecipeRepository {
    override suspend fun getALlFavouriteRecipe(): Result<List<RecipeItem>> {
        return try {
            val list = favouriteRecipeLocalDataSource.getAllFavouriteRecipes()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFavourite(recipeId: Long) {
        favouriteRecipeLocalDataSource.addFavourite(recipeId)
    }

    override suspend fun removeFavourite(recipeId: Long) {
        favouriteRecipeLocalDataSource.removeFavourite(recipeId)
    }
}
