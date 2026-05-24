package com.vadim.expensetracker.di

import com.vadim.expensetracker.data.source.local.IosLocalStorage
import com.vadim.expensetracker.data.source.local.LocalStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformModule(): Module = module {
    single<LocalStorage> { IosLocalStorage() }
}
