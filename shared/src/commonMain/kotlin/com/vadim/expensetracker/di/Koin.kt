package com.vadim.expensetracker.di

import com.vadim.expensetracker.data.repository.ExpenseRepositoryImpl
import com.vadim.expensetracker.data.source.remote.GoogleDriveApi
import com.vadim.expensetracker.domain.repository.ExpenseRepository
import com.vadim.expensetracker.domain.repository.GoogleAuthRepository
import com.vadim.expensetracker.domain.repository.GoogleAuthRepositoryImpl
import com.vadim.expensetracker.presentation.screens.add_expense.AddExpenseViewModel
import com.vadim.expensetracker.presentation.screens.expenses.ExpenseListViewModel
import com.vadim.expensetracker.presentation.screens.settings.SettingsViewModel
import com.vadim.expensetracker.presentation.screens.stats.StatsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
    single { GoogleDriveApi(get()) }
    single<GoogleAuthRepository> { GoogleAuthRepositoryImpl(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get(), get()) }
}

val viewModelModule = module {
    factory { ExpenseListViewModel(get()) }
    factory { AddExpenseViewModel(get()) }
    factory { StatsViewModel(get()) }
    factory { SettingsViewModel(get(), get()) }
}

fun initKoin(appDeclaration: (KoinApplication) -> Unit = {}) {
    startKoin {
        appDeclaration(this)
        modules(dataModule, viewModelModule, getPlatformModule())
    }
}

class KoinHelper : KoinComponent {
    fun getGoogleAuthRepository(): GoogleAuthRepository = get()
}
