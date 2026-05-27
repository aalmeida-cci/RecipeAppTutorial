package com.adrian.recipeapp.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import com.adrian.dailypulsetutorial.db.RecipeAppDatabase

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver =
        NativeSqliteDriver(
            schema = RecipeAppDatabase.Schema.synchronous(),
            name = DB_FILE_NAME,
            onConfiguration = {
                it.copy(
                    extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true)
                )
            }
        )
}