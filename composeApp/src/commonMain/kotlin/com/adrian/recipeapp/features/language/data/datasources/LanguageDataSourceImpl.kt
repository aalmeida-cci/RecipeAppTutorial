package com.adrian.recipeapp.features.language.data.datasources

import androidx.datastore.preferences.core.stringPreferencesKey
import com.adrian.recipeapp.features.common.data.datastore.DataStoreManager
import com.adrian.recipeapp.features.language.domain.entities.AppLang
import kotlinx.coroutines.flow.Flow

class LanguageDataSourceImpl(private val dataStoreManager: DataStoreManager) : LanguageDataSource {

    private val languageKey = stringPreferencesKey("selected_language_code")

    override fun getLangCode(): Flow<String> =
        dataStoreManager.get(languageKey, AppLang.default.code)

    override suspend fun saveLangCode(code: String) {
        dataStoreManager.save(languageKey, code)
    }
}
