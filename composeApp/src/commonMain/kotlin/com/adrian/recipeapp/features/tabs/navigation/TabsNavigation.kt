package com.adrian.recipeapp.features.tabs.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.tabs.ui.TabsRoute

fun NavController.navigateToTabs(navOptions: NavOptions? = null) {
    navigate(Screen.Tabs.route)
}

fun NavGraphBuilder.tabsNavGraph(
    navigateToDetail: (Long) -> Unit,
    navigateToSearch: () -> Unit,
    tabNavController: NavHostController
) {
    composable(Screen.Tabs.route) {
        TabsRoute(
            navigateToDetail = navigateToDetail,
            tabNavController = tabNavController,
            navigateToSearch = navigateToSearch
        )
    }
}
