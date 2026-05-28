package com.adrian.recipeapp.di

import com.adrian.recipeapp.features.common.data.database.DbHelper
import com.adrian.recipeapp.features.common.data.database.daos.FavouriteRecipeDao
import com.adrian.recipeapp.features.common.data.database.daos.RecipeDao
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

fun cacheModule() = module {
    single<CoroutineContext> { Dispatchers.Default }
    single { CoroutineScope(get()) }

    single { DbHelper(get()) }
    single { RecipeDao(get()) }
    single { FavouriteRecipeDao(get()) }
}
