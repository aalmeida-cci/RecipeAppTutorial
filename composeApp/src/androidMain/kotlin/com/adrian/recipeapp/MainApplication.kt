package com.adrian.recipeapp

import android.app.Application
import com.adrian.recipeapp.db.DatabaseDriverFactory
import com.adrian.recipeapp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class MainApplication : Application() {
    private val androidModules =
        module {
            single { DatabaseDriverFactory(applicationContext) }
        }

    override fun onCreate() {
        super.onCreate()
        setupKoin()
    }

    private fun setupKoin() {
        initKoin(additionalModule = listOf(androidModules)) {
            androidContext(applicationContext)
        }
    }
}
