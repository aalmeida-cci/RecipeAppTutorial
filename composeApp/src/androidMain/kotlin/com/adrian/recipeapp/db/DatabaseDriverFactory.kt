package com.adrian.recipeapp.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.adrian.dailypulsetutorial.db.RecipeAppDatabase

actual class DatabaseDriverFactory(private var context: Context) {
    actual suspend fun createDriver(): SqlDriver {
        val schema = RecipeAppDatabase.Schema.synchronous()
        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = DB_FILE_NAME,
            callback = object: AndroidSqliteDriver.Callback(schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            }
        )
    }
}