package com.adrian.recipeapp.di

import com.adrian.recipeapp.features.detail.ui.RecipeDetailViewModel
import org.koin.core.module.dsl.viewModel
import com.adrian.recipeapp.features.feed.ui.FeedViewModel
import org.koin.dsl.module

fun viewModelModule() = module {
    viewModel {
        FeedViewModel(get())
    }
    viewModel {
        RecipeDetailViewModel(get())
    }
}