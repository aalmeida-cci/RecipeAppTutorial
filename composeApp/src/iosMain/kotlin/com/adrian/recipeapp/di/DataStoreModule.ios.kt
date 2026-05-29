package com.adrian.recipeapp.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.adrian.recipeapp.features.common.data.datastore.DataStoreManager
import okio.Path.Companion.toPath
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

fun dataStoreModule() = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath {
            (NSHomeDirectory() + "/app_preferences.preferences_pb").toPath()
        }
    }
    single { DataStoreManager(get()) }
}
