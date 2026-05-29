package com.adrian.recipeapp.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.adrian.recipeapp.features.common.data.datastore.DataStoreManager
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun dataStoreModule() = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath {
            (androidContext().filesDir.path + "/app_preferences.preferences_pb").toPath()
        }
    }
    single { DataStoreManager(get()) }
}
