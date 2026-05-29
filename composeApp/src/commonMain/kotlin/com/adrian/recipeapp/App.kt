@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.adrian.recipeapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.adrian.recipeapp.features.app.data.rememberAppState
import com.adrian.recipeapp.features.app.navigation.AppNavHost
import com.adrian.recipeapp.features.designSystem.theme.RecipeAppTheme
import com.adrian.recipeapp.features.language.ui.LanguageViewModel
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.rememberResourceEnvironment
import org.koin.compose.viewmodel.koinViewModel

@OptIn(InternalResourceApi::class)
@Composable
@Preview
fun App() {
    val langViewModel: LanguageViewModel = koinViewModel()
    val langState by langViewModel.uiState.collectAsStateWithLifecycle()

    val baseEnv = rememberResourceEnvironment()
    val langCode = langState.currentLang.code
    val localizedEnv = remember(langCode, baseEnv) {
        ResourceEnvironment(
            language = LanguageQualifier(langCode),
            region = baseEnv.region,
            theme = baseEnv.theme,
            density = baseEnv.density
        )
    }

    val overriddenComposeEnvironment = remember(localizedEnv) {
        object : ComposeEnvironment {
            @Composable
            override fun rememberEnvironment(): ResourceEnvironment = localizedEnv
        }
    }

    RecipeAppTheme {
        CompositionLocalProvider(
            LocalComposeEnvironment provides overriddenComposeEnvironment
        ) {
            val navController = rememberNavController()
            val appState = rememberAppState(navController)
            AppNavHost(appState = appState)
        }
    }
}
