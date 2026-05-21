package com.adrian.recipeapp.features.feed.data.datasource

import com.adrian.recipeapp.features.common.data.models.RecipeApiItem
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface FeedLocalDataSource {
    suspend fun getRecipesList(): List<RecipeItem>
    suspend fun saveRecipesList(recipes: List<RecipeItem>)
}