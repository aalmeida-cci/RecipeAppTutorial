package com.adrian.recipeapp.features.feed.data.datasource

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface FeedRemoteDataSource {
    suspend fun getRecipesList(): List<RecipeItem>
}
