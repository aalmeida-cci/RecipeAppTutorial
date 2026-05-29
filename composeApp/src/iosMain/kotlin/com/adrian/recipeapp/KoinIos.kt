package com.adrian.recipeapp

import com.adrian.recipeapp.db.DatabaseDriverFactory
import com.adrian.recipeapp.di.dataStoreModule
import com.adrian.recipeapp.di.initKoin
import com.adrian.recipeapp.features.language.data.managers.IosLocaleManager
import com.adrian.recipeapp.features.language.domain.managers.AppLocaleManager
import org.koin.dsl.module

val iosModules =
    module {
        single { DatabaseDriverFactory() }
        single<AppLocaleManager> { IosLocaleManager() }
    }

fun initKoinIOS() = initKoin(additionalModule = listOf(iosModules, dataStoreModule()))
