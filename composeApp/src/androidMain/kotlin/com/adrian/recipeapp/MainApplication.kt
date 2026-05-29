package com.adrian.recipeapp

import android.app.Application
import com.adrian.recipeapp.db.DatabaseDriverFactory
import com.adrian.recipeapp.di.dataStoreModule
import com.adrian.recipeapp.di.initKoin
import com.adrian.recipeapp.features.language.data.managers.AndroidLocaleManager
import com.adrian.recipeapp.features.language.domain.managers.AppLocaleManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class MainApplication : Application() {
    private val androidModules =
        module {
            single { DatabaseDriverFactory(applicationContext) }
            single<AppLocaleManager> { AndroidLocaleManager(applicationContext) }
        }

    override fun onCreate() {
        super.onCreate()
        setupKoin()
    }

    private fun setupKoin() {
        initKoin(additionalModule = listOf(androidModules, dataStoreModule())) {
            androidContext(applicationContext)
        }
    }
}
