package com.vvv.openexpensetracker.data.source.local

interface SecureStorage {
    suspend fun saveString(key: String, value: String?)
    suspend fun getString(key: String): String?
    suspend fun clear()
}
