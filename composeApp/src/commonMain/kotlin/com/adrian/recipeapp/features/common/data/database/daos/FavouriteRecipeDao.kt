package com.adrian.recipeapp.features.common.data.database.daos

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.adrian.recipeapp.features.common.data.database.DbHelper
import com.adrian.recipeapp.features.common.data.database.recipeEntityMapper
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem
import kotlin.text.category
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FavouriteRecipeDao(
    private val dbHelper: DbHelper
) {
    suspend fun addFavourite(recipeId: Long) {
        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        dbHelper.withDatabase { database ->
            database.favouriteRecipeQueries.upsertRecipe(
                recipe_id = recipeId,
                added_at = currentTime.toString()
            ).await()
        }
    }

    suspend fun removeFavourite(recipeId: Long) {
        dbHelper.withDatabase { database ->
            database.favouriteRecipeQueries.deleteFavouriteRecipeById(
                recipe_id = recipeId
            )
        }
    }

    suspend fun getAllFavouriteRecipes(): List<RecipeItem> {
        return dbHelper.withDatabase { database ->
            database.favouriteRecipeQueries
                .selectAllFavouritesRecipes()
                .awaitAsList()
                .map { recipe ->
                    recipeEntityMapper(recipe)
                }
        }
    }

    suspend fun isFavourite(recipeId: Long): Boolean {
        return dbHelper.withDatabase { database ->
            database.favouriteRecipeQueries
                .selectFavouriteRecipeById(recipeId)
                .awaitAsOneOrNull() != null
        }
    }

    suspend fun insertRecipesBulk(recipes: List<RecipeItem>) {
        dbHelper.withDatabase { database ->
            recipes.forEach { recipeItem ->
                database.recipeEntityQueries.insertRecipe(
                    recipeItem.id,
                    recipeItem.title,
                    recipeItem.description,
                    recipeItem.category,
                    recipeItem.area,
                    recipeItem.imageUrl,
                    recipeItem.youtubeLink,
                    recipeItem.ingredients,
                    recipeItem.instructions,
                    if (recipeItem.isFavorite) 1 else 0,
                    recipeItem.rating,
                    recipeItem.duration,
                    recipeItem.difficulty
                )
            }
        }
    }

    suspend fun upsertRecipesBulk(recipes: List<RecipeItem>) {
        dbHelper.withDatabase { database ->
            recipes.forEach { recipeItem ->
                database.recipeEntityQueries.upsertRecipe(
                    recipeItem.title,
                    recipeItem.description,
                    recipeItem.category,
                    recipeItem.area,
                    recipeItem.imageUrl,
                    recipeItem.youtubeLink,
                    recipeItem.ingredients,
                    recipeItem.instructions,
                    if (recipeItem.isFavorite) 1 else 0,
                    recipeItem.rating,
                    recipeItem.duration,
                    recipeItem.difficulty,
                    recipeItem.id
                )
            }
        }
    }

    suspend fun getAllRecipes(): List<RecipeItem> {
        return dbHelper.withDatabase { database ->
            database.recipeEntityQueries.selectAllRecipes().awaitAsList().map {
                recipeEntityMapper(it)
            }
        }
    }

    suspend fun getRecipeById(id: Long): RecipeItem? {
        return dbHelper.withDatabase { database ->
            database.recipeEntityQueries.selectRecipeById(id).awaitAsOneOrNull()?.let {
                recipeEntityMapper(it)
            }
        }
    }

    suspend fun deleteRecipeById(id: Long) {
        dbHelper.withDatabase { database ->
            database.recipeEntityQueries.deleteRecipeById(id)
        }
    }
}
