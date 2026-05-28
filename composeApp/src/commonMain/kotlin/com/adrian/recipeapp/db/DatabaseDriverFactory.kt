package com.adrian.recipeapp.db

import app.cash.sqldelight.db.SqlDriver

const val DB_FILE_NAME = "RecipeAppDatabase.db"

expect class DatabaseDriverFactory {
    suspend fun createDriver(): SqlDriver
}
