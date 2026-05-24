package com.vadim.expensetracker

import android.app.Application
import com.vadim.expensetracker.di.initKoin
import org.koin.android.ext.koin.androidContext

class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ExpenseTrackerApp)
        }
    }
}
