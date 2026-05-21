package com.adrian.recipeapp.di

import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailLocalDataSource
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailLocalDataSourceImpl
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailRemoteDataSource
import com.adrian.recipeapp.features.detail.data.datasources.RecipeDetailRemoteDataSourceImpl
import com.adrian.recipeapp.features.detail.data.repository.RecipeDetailRepositoryImpl
import com.adrian.recipeapp.features.detail.repositories.RecipeDetailRepository
import com.adrian.recipeapp.features.feed.data.datasource.FeedLocalDataSource
import com.adrian.recipeapp.features.feed.data.datasource.FeedLocalDataSourceImpl
import com.adrian.recipeapp.features.feed.data.datasource.FeedRemoteDataSource
import com.adrian.recipeapp.features.feed.data.datasource.FeedRemoteDataSourceImpl
import com.adrian.recipeapp.features.feed.data.repositories.FeedRepositoryImpl
import com.adrian.recipeapp.features.feed.domain.repositories.FeedRepository
import org.koin.dsl.module

fun dataModule() = module {
    single<FeedLocalDataSource>  { FeedLocalDataSourceImpl(get()) }
    single<FeedRemoteDataSource>  { FeedRemoteDataSourceImpl(get()) }
    single<FeedRepository> { FeedRepositoryImpl(get(), get()) }


    single<RecipeDetailLocalDataSource>  { RecipeDetailLocalDataSourceImpl(get()) }
    single<RecipeDetailRemoteDataSource>  { RecipeDetailRemoteDataSourceImpl(get()) }
    single<RecipeDetailRepository> { RecipeDetailRepositoryImpl(get(), get()) }
}