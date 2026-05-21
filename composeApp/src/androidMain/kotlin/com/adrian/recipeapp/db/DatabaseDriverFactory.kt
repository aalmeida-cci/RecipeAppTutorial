package com.adrian.recipeapp.db

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.adrian.dailypulsetutorial.db.RecipeAppDatabase

actual class DatabaseDriverFactory(private var context: Context) {
    actual suspend fun createDriver(): SqlDriver =
        AndroidSqliteDriver(
            schema = RecipeAppDatabase.Schema.synchronous(),
            context = context,
            name = DB_FILE_NAME
        )
}