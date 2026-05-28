package com.adrian.recipeapp.di

import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailLocalDataSource
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailLocalDataSourceImpl
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailRemoteDataSource
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailRemoteDataSourceImpl
import com.adrian.recipeapp.features.detail.data.repository.RecipeDetailRepositoryImpl
import com.adrian.recipeapp.features.detail.repositories.RecipeDetailRepository
import com.adrian.recipeapp.features.favourites.data.datasource.FavouriteRecipeLocalDataSource
import com.adrian.recipeapp.features.favourites.data.datasource.FavouriteRecipeLocalDataSourceImpl
import com.adrian.recipeapp.features.favourites.data.repositories.FavouriteRecipeRepository
import com.adrian.recipeapp.features.favourites.data.repositories.FavouriteRecipeRepositoryImpl
import com.adrian.recipeapp.features.feed.data.datasource.FeedLocalDataSource
import com.adrian.recipeapp.features.feed.data.datasource.FeedLocalDataSourceImpl
import com.adrian.recipeapp.features.feed.data.datasource.FeedRemoteDataSource
import com.adrian.recipeapp.features.feed.data.datasource.FeedRemoteDataSourceImpl
import com.adrian.recipeapp.features.feed.data.repositories.FeedRepositoryImpl
import com.adrian.recipeapp.features.feed.domain.repositories.FeedRepository
import com.adrian.recipeapp.features.search.data.datasources.SearchLocalDataSource
import com.adrian.recipeapp.features.search.data.datasources.SearchLocalDataSourceImpl
import com.adrian.recipeapp.features.search.data.repositories.SearchRepositoryImpl
import com.adrian.recipeapp.features.search.domain.repositories.SearchRepository
import org.koin.dsl.module

fun dataModule() = module {
    //Feed
    single<FeedLocalDataSource>  { FeedLocalDataSourceImpl(get()) }
    single<FeedRemoteDataSource>  { FeedRemoteDataSourceImpl(get()) }
    single<FeedRepository> { FeedRepositoryImpl(get(), get()) }

    //Recipe detail
    single<RecipeDetailLocalDataSource>  { RecipeDetailLocalDataSourceImpl(get(), get()) }
    single<RecipeDetailRemoteDataSource>  { RecipeDetailRemoteDataSourceImpl(get()) }
    single<RecipeDetailRepository> { RecipeDetailRepositoryImpl(get(), get()) }

    //Favourite recipe
    single<FavouriteRecipeLocalDataSource> { FavouriteRecipeLocalDataSourceImpl(get()) }
    single<FavouriteRecipeRepository> { FavouriteRecipeRepositoryImpl(get()) }

    //Search
    single<SearchLocalDataSource> { SearchLocalDataSourceImpl(get()) }
    single<SearchRepository> { SearchRepositoryImpl(get()) }
}