package com.vvv.openexpensetracker.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.vvv.openexpensetracker.core.Constants

class AndroidLocalStorage(private val context: Context) : LocalStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun getFilesDir(): String {
        return context.filesDir.absolutePath
    }

    override val fileSystem: okio.FileSystem = okio.FileSystem.SYSTEM
}
