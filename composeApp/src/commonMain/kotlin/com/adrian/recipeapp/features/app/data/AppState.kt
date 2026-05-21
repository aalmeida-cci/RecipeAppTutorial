package com.adrian.recipeapp.features.app.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.adrian.recipeapp.features.tabs.navigation.navigateToTabs
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberAppState(
    navController: NavHostController,
    scope: CoroutineScope = rememberCoroutineScope()
): AppState {
    return remember(
        navController,
        scope
    ) {
        AppState(navController, scope)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    scope: CoroutineScope
) {
    fun navigateToTabs() = navController.navigateToTabs()
}