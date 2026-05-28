package com.adrian.recipeapp.di

import com.adrian.recipeapp.features.common.data.api.httpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun networkModule() = module {
    single<HttpClient> {
        httpClient
    }
}
