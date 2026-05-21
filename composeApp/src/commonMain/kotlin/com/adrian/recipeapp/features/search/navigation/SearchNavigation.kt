package com.adrian.recipeapp.features.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.search.ui.SearchRoute

fun NavController.navigateToSearch(navOptions: NavOptions ? = null) {
    navigate(Screen.Search.route)
}

fun NavGraphBuilder.searchNavGraph() {
    composable(Screen.Search.route) {
        SearchRoute()
    }
}