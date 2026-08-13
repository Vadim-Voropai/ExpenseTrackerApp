package com.vvv.openexpensetracker

import android.app.Application
import com.vvv.openexpensetracker.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ExpenseTrackerApp)
            androidLogger()
        }


    }
}
