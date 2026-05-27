package com.adrian.recipeapp.features.detail.data.repository

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailLocalDataSource
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailRemoteDataSource
import com.adrian.recipeapp.features.detail.repositories.RecipeDetailRepository

class RecipeDetailRepositoryImpl(
    private val recipeDetailLocalDataSource: RecipeDetailLocalDataSource,
    private val recipeDetailRemoteDataSource: RecipeDetailRemoteDataSource
): RecipeDetailRepository {
    override suspend fun getRecipesDetail(id: Long): Result<RecipeItem> {
        return try {
            val recipeDetailFromDb = recipeDetailLocalDataSource.getRecipeDetail(id = id)

            return if (recipeDetailFromDb != null) {
                val isFav = recipeDetailLocalDataSource.isFavourite(recipeId = id)
                print("RecipeDetailRepository -> Recipe -> isFav: $isFav")
                Result.success(recipeDetailFromDb.copy(
                    isFavorite = isFav
                ))
            } else {
                val recipeDetailApiResponse =
                    recipeDetailRemoteDataSource.getRecipeDetail(id = id) ?: return Result.failure(Exception("Recipe not found"))
                recipeDetailLocalDataSource.saveRecipe(recipeDetailApiResponse)
                Result.success(recipeDetailApiResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFavourite(recipeId: Long) {
        recipeDetailLocalDataSource.addFavourite(recipeId)
    }

    override suspend fun removeFavourite(recipeId: Long) {
        recipeDetailLocalDataSource.removeFavourite(recipeId)
    }
}