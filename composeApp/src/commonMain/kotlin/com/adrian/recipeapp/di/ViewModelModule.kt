package com.adrian.recipeapp.di

import com.adrian.recipeapp.features.detail.ui.RecipeDetailViewModel
import com.adrian.recipeapp.features.favourites.ui.FavouriteScreenViewModel
import org.koin.core.module.dsl.viewModel
import com.adrian.recipeapp.features.feed.ui.FeedViewModel
import com.adrian.recipeapp.features.search.ui.SearchViewModel
import org.koin.dsl.module

fun viewModelModule() = module {
    viewModel {
        FeedViewModel(get())
    }
    viewModel {
        RecipeDetailViewModel(get())
    }
    viewModel {
        FavouriteScreenViewModel(get())
    }
    viewModel {
        SearchViewModel(get())
    }
}