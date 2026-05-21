package com.adrian.recipeapp.features.favourites.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.favourites.ui.FavouritesRoute

fun NavController.navigateToFavourites(navOptions: NavOptions ? = null) {
    navigate(Screen.Favorites.route)
}

fun NavGraphBuilder.favouritesNavGraph() {
    composable(Screen.Favorites.route) {
        FavouritesRoute()
    }
}