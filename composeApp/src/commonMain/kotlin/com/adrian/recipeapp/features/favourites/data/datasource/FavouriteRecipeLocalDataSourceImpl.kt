package com.adrian.recipeapp.features.favourites.data.datasource

import com.adrian.recipeapp.features.common.data.database.daos.FavouriteRecipeDao
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

class FavouriteRecipeLocalDataSourceImpl(
    private val favouriteRecipeDao: FavouriteRecipeDao
) : FavouriteRecipeLocalDataSource {
    override suspend fun getAllFavouriteRecipes(): List<RecipeItem> {
        return favouriteRecipeDao.getAllFavouriteRecipes()
    }

    override suspend fun addFavourite(recipeId: Long) {
        favouriteRecipeDao.addFavourite(recipeId)
    }

    override suspend fun removeFavourite(recipeId: Long) {
        favouriteRecipeDao.removeFavourite(recipeId)
    }
}
