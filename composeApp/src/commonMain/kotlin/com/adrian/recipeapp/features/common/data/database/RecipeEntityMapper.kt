package com.adrian.recipeapp.features.common.data.database

import com.adrian.recipeapp.db.Recipe
import com.adrian.recipeapp.features.common.domain.entities.RecipeItem

fun recipeEntityMapper(recipe: Recipe)  = RecipeItem(
    recipe.id,
    recipe.title,
    recipe.description,
    recipe.category,
    recipe.area,
    recipe.imageUrl,
    recipe.youtubeLink,
    recipe.ingredients,
    recipe.instructions,
    recipe.isFavorite == 1L,
    recipe.rating,
    recipe.duration ?: "20 Mins",
    recipe.difficulty ?: "Easy"
)