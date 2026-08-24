package com.vvv.openexpensetracker.data.source.local

interface LocalStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun saveExpensesFile(content: String)
    fun loadExpensesFile(): String?
    fun getFilesDir(): String
    val fileSystem: okio.FileSystem
}
