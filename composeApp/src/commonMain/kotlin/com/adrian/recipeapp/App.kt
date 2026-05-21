package com.adrian.recipeapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.adrian.recipeapp.features.app.data.rememberAppState
import com.adrian.recipeapp.features.app.navigation.AppNavHost
import com.adrian.recipeapp.features.designSystem.theme.RecipeAppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext

import recipeapp.composeapp.generated.resources.Res
import recipeapp.composeapp.generated.resources.app_name
import recipeapp.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    RecipeAppTheme {
        //KoinContext {
        val navController = rememberNavController()
        val appState = rememberAppState(navController)

        AppNavHost(
            appState = appState
        )
        // }
    }
}