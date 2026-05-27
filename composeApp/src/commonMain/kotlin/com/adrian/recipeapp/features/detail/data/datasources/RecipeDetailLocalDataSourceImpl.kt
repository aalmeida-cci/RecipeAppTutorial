package com.adrian.recipeapp.features.detail.data.datasources

import com.adrian.recipeapp.features.common.data.database.daos.FavouriteRecipeDao
import com.adrian.recipeapp.features.common.data.database.daos.RecipeDao
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

class RecipeDetailLocalDataSourceImpl(
    private val recipeDao: RecipeDao,
    private val favouriteDao: FavouriteRecipeDao
): RecipeDetailLocalDataSource {


    override suspend fun getRecipeDetail(id: Long): RecipeItem? {
        return recipeDao.getRecipeById(id)
    }

    override suspend fun saveRecipe(recipe: RecipeItem) {
        recipeDao.insertRecipe(recipe)
    }

    override suspend fun addFavourite(id: Long) {
        favouriteDao.addFavourite(id)
    }

    override suspend fun removeFavourite(recipeId: Long) {
        favouriteDao.removeFavourite(recipeId)
    }

    override suspend fun isFavourite(recipeId: Long): Boolean {
        return favouriteDao.isFavourite(recipeId)
    }
}