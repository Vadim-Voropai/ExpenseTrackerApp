package com.vvv.openexpensetracker.di

import com.vvv.openexpensetracker.data.source.local.AndroidLocalStorage
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.data.source.local.db.DriverFactory
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getPlatformModule(): Module = module {
    single { DriverFactory(get()) }
    single<LocalStorage> { AndroidLocalStorage(get()) }
    single<GoogleAuthRepository> { GoogleAuthRepositoryImpl(get(), get()) }
}
