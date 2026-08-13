package com.vvv.openexpensetracker.di

import com.vvv.openexpensetracker.data.repository.ExpenseRepositoryImpl
import com.vvv.openexpensetracker.data.repository.PreferencesRepositoryImpl
import com.vvv.openexpensetracker.data.source.local.db.DriverFactory
import com.vvv.openexpensetracker.data.source.remote.GoogleDriveApi
import com.vvv.openexpensetracker.db.AppDatabase
import com.vvv.openexpensetracker.domain.repository.ExpenseRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import com.vvv.openexpensetracker.presentation.MainViewModel
import com.vvv.openexpensetracker.presentation.screens.add_expense.AddExpenseViewModel
import com.vvv.openexpensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vvv.openexpensetracker.presentation.screens.settings.SettingsViewModel
import com.vvv.openexpensetracker.presentation.screens.stats.StatsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun getPlatformModule(): Module

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
        }
    }
    single { AppDatabase(get<DriverFactory>().createDriver()) }
    single { GoogleDriveApi(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get(), get(), get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl() }
}

val viewModelModule = module {
    factory { ExpenseListViewModel(get(), preferencesRepository = get()) }
    factory { AddExpenseViewModel(get(), preferencesRepository = get()) }
    factory { StatsViewModel(get()) }
    factory { SettingsViewModel(get(), get(), get()) }
    factory { MainViewModel(get()) }
}

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(dataModule, viewModelModule, getPlatformModule())
    }
}

class KoinHelper : KoinComponent {
    fun getGoogleAuthRepository(): GoogleAuthRepository = get()
}
