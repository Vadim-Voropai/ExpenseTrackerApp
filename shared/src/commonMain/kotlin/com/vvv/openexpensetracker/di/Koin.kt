package com.vvv.openexpensetracker.di

import com.vvv.openexpensetracker.data.repository.ExpenseRepositoryImpl
import com.vvv.openexpensetracker.data.repository.GoogleDriveRepositoryImpl
import com.vvv.openexpensetracker.data.repository.PreferencesRepositoryImpl
import com.vvv.openexpensetracker.data.source.local.db.DriverFactory
import com.vvv.openexpensetracker.data.source.remote.GoogleDriveApi
import com.vvv.openexpensetracker.db.AppDatabase
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.GoogleDriveRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import com.vvv.openexpensetracker.presentation.MainViewModel
import com.vvv.openexpensetracker.presentation.screens.add_expense.AddExpenseViewModel
import com.vvv.openexpensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vvv.openexpensetracker.presentation.screens.settings.SettingsViewModel
import com.vvv.openexpensetracker.presentation.screens.stats.StatsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect fun getPlatformModule(): Module

val dataModule = module {
    single { Json { ignoreUnknownKeys = true } }
    
    // Base client for Auth repository (no Auth plugin to avoid circular dependency)
    single(named("baseHttpClient")) {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>(), contentType = ContentType.Any)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KTOR_HTTP_BASE: $message")
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    // Default client with automatic token handling and refresh
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get<Json>(), contentType = ContentType.Any)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KTOR_HTTP_AUTH: $message")
                    }
                }
                level = LogLevel.ALL
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val authRepository = get<GoogleAuthRepository>()
                        val token = authRepository.accessToken.value
                        if (token != null) BearerTokens(token, "") else null
                    }
                    refreshTokens {
                        val authRepository = get<GoogleAuthRepository>()
                        val newToken = authRepository.refreshAccessToken()
                        if (newToken != null) BearerTokens(newToken, "") else null
                    }
                }
            }
        }
    }
    
    single { AppDatabase(get<DriverFactory>().createDriver()) }
    single { GoogleDriveApi(get()) }
    single<GoogleDriveRepository> { GoogleDriveRepositoryImpl(get(), get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get(), get(), get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl() }
}

val viewModelModule = module {
    factory { ExpenseListViewModel(get(), preferencesRepository = get()) }
    factory { AddExpenseViewModel(get(), preferencesRepository = get()) }
    factory { StatsViewModel(get(), preferencesRepository = get()) }
    factory { SettingsViewModel(get(), get(), get()) }
    factory { MainViewModel(get(), get()) }
}

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(dataModule, viewModelModule, getPlatformModule())
    }
}
