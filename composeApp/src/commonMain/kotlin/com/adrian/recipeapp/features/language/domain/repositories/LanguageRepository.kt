package com.adrian.recipeapp.features.language.domain.repositories

import com.adrian.recipeapp.features.language.domain.entities.AppLang
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getSelectedLang(): Flow<Result<AppLang>>
    suspend fun saveSelectedLang(lang: AppLang): Result<Unit>
}
