package com.adrian.recipeapp.features.language.data.managers

import com.adrian.recipeapp.features.language.domain.managers.AppLocaleManager

class IosLocaleManager : AppLocaleManager {
    override fun applyLocale(code: String) {
        // iOS locale handled by CMP LocalComposeEnvironment in App.kt
    }
}
