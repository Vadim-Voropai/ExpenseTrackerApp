package com.vvv.openexpensetracker.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.vvv.openexpensetracker.core.Constants
import java.io.File

class AndroidLocalStorage(private val context: Context) : LocalStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun saveExpensesFile(content: String) {
        val file = File(context.filesDir, Constants.EXPENSES_FILE_NAME)
        file.writeText(content)
    }

    override fun loadExpensesFile(): String? {
        val file = File(context.filesDir, Constants.EXPENSES_FILE_NAME)
        return if (file.exists()) file.readText() else null
    }

    override fun getFilesDir(): String {
        return context.filesDir.absolutePath
    }

    override val fileSystem: okio.FileSystem = okio.FileSystem.SYSTEM
}
