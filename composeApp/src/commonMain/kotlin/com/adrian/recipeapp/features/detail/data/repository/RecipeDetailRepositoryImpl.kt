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
                Result.success(recipeDetailFromDb)
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

}