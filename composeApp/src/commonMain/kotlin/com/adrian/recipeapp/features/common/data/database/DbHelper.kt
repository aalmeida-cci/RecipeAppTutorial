package com.adrian.recipeapp.features.common.data.database

import com.adrian.dailypulsetutorial.db.RecipeAppDatabase
import com.adrian.recipeapp.db.DatabaseDriverFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DbHelper(
    private val driverFactory: DatabaseDriverFactory
) {
    private var db: RecipeAppDatabase? = null
    private val mutex = Mutex()

    suspend fun <Result : Any?> withDatabase(block: suspend (RecipeAppDatabase) -> Result) =
        mutex.withLock {
            if (db == null) {
                db = createDb(driverFactory)
            }

            return@withLock block(db!!)
        }

    private suspend fun createDb(driverFactory: DatabaseDriverFactory): RecipeAppDatabase {
        return RecipeAppDatabase(
            driver = driverFactory.createDriver(),
            RecipeAdapter =
            com.adrian.recipeapp.db.Recipe.Adapter(
                ingredientsAdapter = listOfStringsAdapter,
                instructionsAdapter = listOfStringsAdapter
            )
        )
    }
}
