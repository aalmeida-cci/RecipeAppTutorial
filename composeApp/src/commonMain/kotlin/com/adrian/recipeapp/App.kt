package com.adrian.recipeapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.adrian.recipeapp.features.app.data.rememberAppState
import com.adrian.recipeapp.features.app.navigation.AppNavHost
import com.adrian.recipeapp.features.designSystem.theme.RecipeAppTheme

@Composable
@Preview
fun App() {
    RecipeAppTheme {
        // KoinContext {
        val navController = rememberNavController()
        val appState = rememberAppState(navController)

        AppNavHost(
            appState = appState
        )
        // }
    }
}
