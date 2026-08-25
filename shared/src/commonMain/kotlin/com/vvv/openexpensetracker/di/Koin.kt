package com.vvv.openexpensetracker.di

import com.vvv.openexpensetracker.data.repository.ExpenseRepositoryImpl
import com.vvv.openexpensetracker.data.repository.GoogleDriveRepositoryImpl
import com.vvv.openexpensetracker.data.repository.LlmRepositoryImpl
import com.vvv.openexpensetracker.data.repository.PreferencesRepositoryImpl
import com.vvv.openexpensetracker.data.source.local.db.DriverFactory
import com.vvv.openexpensetracker.data.source.remote.GoogleDriveApi
import com.vvv.openexpensetracker.db.AppDatabase
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.GoogleDriveRepository
import com.vvv.openexpensetracker.domain.repository.LlmRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import com.vvv.openexpensetracker.domain.usecase.AnalyzeReceiptLlmUseCase
import com.vvv.openexpensetracker.domain.usecase.DeleteExpenseUseCase
import com.vvv.openexpensetracker.domain.usecase.DeleteLlmModelUseCase
import com.vvv.openexpensetracker.domain.usecase.DownloadLlmModelUseCase
import com.vvv.openexpensetracker.domain.usecase.GetAuthStateUseCase
import com.vvv.openexpensetracker.domain.usecase.GetCurrencyUseCase
import com.vvv.openexpensetracker.domain.usecase.GetExpensesUseCase
import com.vvv.openexpensetracker.domain.usecase.GetLastSyncTimeUseCase
import com.vvv.openexpensetracker.domain.usecase.GetLlmStatusUseCase
import com.vvv.openexpensetracker.domain.usecase.HandleSignInResultUseCase
import com.vvv.openexpensetracker.domain.usecase.SaveExpenseUseCase
import com.vvv.openexpensetracker.domain.usecase.SetCurrencyUseCase
import com.vvv.openexpensetracker.domain.usecase.SignInUseCase
import com.vvv.openexpensetracker.domain.usecase.SignOutUseCase
import com.vvv.openexpensetracker.domain.usecase.SyncExpensesUseCase
import com.vvv.openexpensetracker.domain.usecase.UndoDeleteExpenseUseCase
import com.vvv.openexpensetracker.presentation.MainViewModel
import com.vvv.openexpensetracker.presentation.screens.add_expense.AddExpenseViewModel
import com.vvv.openexpensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vvv.openexpensetracker.presentation.screens.scan_receipt.ScanReceiptViewModel
import com.vvv.openexpensetracker.presentation.screens.settings.SettingsViewModel
import com.vvv.openexpensetracker.presentation.screens.stats.StatsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
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

    // Specialized client for large file downloads (no logging of body to avoid OOM)
    single(named("downloadHttpClient")) {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 600000 // 10 minutes
                connectTimeoutMillis = 60000
                socketTimeoutMillis = 600000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("KTOR_HTTP_DOWNLOAD: $message")
                    }
                }
                level = LogLevel.HEADERS
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
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
    single<LlmRepository> { LlmRepositoryImpl(get(named("downloadHttpClient")), get(), get()) }
}

val useCaseModule = module {
    single { GetExpensesUseCase(get()) }
    single { SaveExpenseUseCase(get()) }
    single { DeleteExpenseUseCase(get()) }
    single { UndoDeleteExpenseUseCase(get()) }
    single { SyncExpensesUseCase(get()) }
    single { GetLastSyncTimeUseCase(get()) }
    
    single { SignInUseCase(get()) }
    single { SignOutUseCase(get()) }
    single { GetAuthStateUseCase(get()) }
    single { HandleSignInResultUseCase(get()) }
    
    single { GetCurrencyUseCase(get()) }
    single { SetCurrencyUseCase(get()) }

    single { GetLlmStatusUseCase(get()) }
    single { DownloadLlmModelUseCase(get()) }
    single { DeleteLlmModelUseCase(get()) }
    single { AnalyzeReceiptLlmUseCase(get()) }
}

val viewModelModule = module {
    factory {
        ExpenseListViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory {
        AddExpenseViewModel(
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory {
        StatsViewModel(
            get(),
            get(),
        )
    }
    factory {
        SettingsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory {
        ScanReceiptViewModel(
            get(),
        )
    }
    factory {
        MainViewModel(
            get(),
            get(),
            get(),
            get(),
        )
    }
}

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(dataModule, useCaseModule, viewModelModule, getPlatformModule())
    }
}
