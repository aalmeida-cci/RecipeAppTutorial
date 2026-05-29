package com.adrian.recipeapp.features.language.data.repositories

import com.adrian.recipeapp.features.language.data.datasources.LanguageDataSource
import com.adrian.recipeapp.features.language.domain.entities.AppLang
import com.adrian.recipeapp.features.language.domain.repositories.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LanguageRepositoryImpl(private val dataSource: LanguageDataSource) : LanguageRepository {

    override fun getSelectedLang(): Flow<Result<AppLang>> = dataSource.getLangCode().map { code ->
        runCatching { AppLang.fromCode(code) }
    }

    override suspend fun saveSelectedLang(lang: AppLang): Result<Unit> =
        runCatching { dataSource.saveLangCode(lang.code) }
}
