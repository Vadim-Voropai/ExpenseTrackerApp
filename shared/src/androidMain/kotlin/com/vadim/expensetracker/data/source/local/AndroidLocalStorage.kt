package com.vadim.expensetracker.data.source.local

import android.content.Context
import android.content.SharedPreferences
import java.io.File

class AndroidLocalStorage(private val context: Context) : LocalStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun saveExpensesFile(content: String) {
        val file = File(context.filesDir, "expenses.json")
        file.writeText(content)
    }

    override fun loadExpensesFile(): String? {
        val file = File(context.filesDir, "expenses.json")
        return if (file.exists()) file.readText() else null
    }
}
