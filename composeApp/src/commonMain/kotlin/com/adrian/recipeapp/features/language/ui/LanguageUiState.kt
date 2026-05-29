package com.adrian.recipeapp.features.language.ui

import com.adrian.recipeapp.features.language.domain.entities.AppLang

data class LanguageUiState(
    val currentLang: AppLang = AppLang.default,
    val pendingLang: AppLang = AppLang.default,
    val isBottomSheetVisible: Boolean = false,
    val isLoading: Boolean = false
)
