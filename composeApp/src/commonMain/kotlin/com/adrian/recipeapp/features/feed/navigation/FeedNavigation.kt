package com.adrian.recipeapp.features.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.feed.ui.FeedRoute

fun NavController.navigateToFeed(navOptions: NavOptions? = null) {
    navigate(Screen.Home.route)
}

fun NavGraphBuilder.feedNavGraph(navigateToSearch: () -> Unit, navigateToDetail: (Long) -> Unit) {
    composable(Screen.Home.route) {
        FeedRoute(
            navigateToSearch = navigateToSearch,
            navigateToDetail = navigateToDetail
        )
    }
}
