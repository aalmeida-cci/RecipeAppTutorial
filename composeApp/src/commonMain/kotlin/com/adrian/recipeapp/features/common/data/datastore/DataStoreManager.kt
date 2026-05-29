package com.adrian.recipeapp.features.common.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreManager(private val dataStore: DataStore<Preferences>) {
    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    fun <T> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[key] ?: defaultValue }
}
