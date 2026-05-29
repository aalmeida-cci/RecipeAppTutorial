package com.adrian.recipeapp.features.language.domain.entities

import org.jetbrains.compose.resources.StringResource
import recipeapp.composeapp.generated.resources.Res
import recipeapp.composeapp.generated.resources.english
import recipeapp.composeapp.generated.resources.french

enum class AppLang(val code: String, val displayNameRes: StringResource) {
    EN("en", Res.string.english),
    FR("fr", Res.string.french);

    companion object {
        val default: AppLang = EN

        fun fromCode(code: String): AppLang = entries.firstOrNull { it.code == code } ?: default
    }
}
