package com.adrian.recipeapp.features.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.adrian.recipeapp.features.app.data.AppState
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.detail.navigation.detailNavGraph
import com.adrian.recipeapp.features.search.navigation.searchNavGraph
import com.adrian.recipeapp.features.tabs.navigation.tabsNavGraph

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    appState: AppState,
    startDestination: String = Screen.Tabs.route
) {
    val navController = appState.navController
    val tabNavController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        tabsNavGraph(
            tabNavController = tabNavController,
            navigateToDetail = {
                appState.navigateToDetail(it)
            }
        )
        searchNavGraph()
        detailNavGraph(onBackClick = appState::navigateBack)
    }
}