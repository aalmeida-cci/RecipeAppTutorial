package com.adrian.recipeapp.features.language.data.managers

import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.adrian.recipeapp.features.language.domain.managers.AppLocaleManager

class AndroidLocaleManager(private val context: Context) : AppLocaleManager {
    override fun applyLocale(code: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(code)
        }
        // API < 33: locale handled by CMP LocalComposeEnvironment in App.kt
    }
}
