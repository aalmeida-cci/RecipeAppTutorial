package com.adrian.recipeapp

import com.adrian.recipeapp.db.DatabaseDriverFactory
import com.adrian.recipeapp.di.initKoin
import org.koin.dsl.module

val iosModules =
    module {
        single { DatabaseDriverFactory() }
    }

fun initKoinIOS() = initKoin(additionalModule = listOf(iosModules))
