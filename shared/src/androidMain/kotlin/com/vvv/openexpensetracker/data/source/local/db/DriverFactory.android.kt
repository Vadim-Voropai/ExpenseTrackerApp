package com.vvv.openexpensetracker.data.source.local.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.vvv.openexpensetracker.db.AppDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "expenses.db",
            callback = object : AndroidSqliteDriver.Callback(AppDatabase.Schema) {
                override fun onDowngrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    // Logic to handle downgrade - dropping all tables and recreating
                    db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                        while (cursor.moveToNext()) {
                            val tableName = cursor.getString(0)
                            if (tableName != "android_metadata" && tableName != "sqlite_sequence") {
                                db.execSQL("DROP TABLE IF EXISTS $tableName")
                            }
                        }
                    }
                    onCreate(db)
                }
            }
        )
    }
}
