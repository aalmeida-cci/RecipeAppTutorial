package com.adrian.recipeapp.features.feed.domain.repositories

import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

interface FeedRepository {
    suspend fun getRecipesList(): Result<List<RecipeItem>>
}
