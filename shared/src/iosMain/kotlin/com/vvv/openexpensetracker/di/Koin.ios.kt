package com.vvv.openexpensetracker.di

import com.vvv.openexpensetracker.data.source.local.IosLocalStorage
import com.vvv.openexpensetracker.data.source.local.IosSecureStorage
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.data.source.local.SecureStorage
import com.vvv.openexpensetracker.data.source.local.db.DriverFactory
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepository
import com.vvv.openexpensetracker.domain.repository.GoogleAuthRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual fun getPlatformModule(): Module = module {
    single { DriverFactory() }
    single<LocalStorage> { IosLocalStorage() }
    single<SecureStorage> { IosSecureStorage() }
    single<GoogleAuthRepository> { GoogleAuthRepositoryImpl(get(named("baseHttpClient")), get()) }
}
