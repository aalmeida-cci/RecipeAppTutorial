package com.adrian.recipeapp.features.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.splash.ui.SplashRoute

fun NavGraphBuilder.splashNavGraph(onSplashComplete: () -> Unit) {
    composable(Screen.Splash.route) {
        SplashRoute(onSplashComplete = onSplashComplete)
    }
}
