package com.adrian.recipeapp.features.language.data.datasources

import kotlinx.coroutines.flow.Flow

interface LanguageDataSource {
    fun getLangCode(): Flow<String>
    suspend fun saveLangCode(code: String)
}
