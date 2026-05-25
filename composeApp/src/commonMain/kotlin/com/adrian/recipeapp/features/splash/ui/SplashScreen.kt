package com.adrian.recipeapp.features.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import recipeapp.composeapp.generated.resources.Res
import recipeapp.composeapp.generated.resources.recipe_app_logo

@Composable
fun SplashRoute(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3_000L)
        onSplashComplete()
    }
    SplashScreen()
}

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.recipe_app_logo),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
    }
}
