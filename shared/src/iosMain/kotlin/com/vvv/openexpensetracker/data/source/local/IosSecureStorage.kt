package com.vvv.openexpensetracker.data.source.local

import platform.Foundation.*
import platform.Security.*
import kotlinx.cinterop.*

class IosSecureStorage : SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveString(key: String, value: String?) {
        if (value == null) {
            defaults.removeObjectForKey(key)
        } else {
            defaults.setObject(value, forKey = key)
        }
    }

    override suspend fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override suspend fun clear() {
        val dictionary = defaults.dictionaryRepresentation()
        dictionary.keys.forEach { key ->
            if (key is String) {
                defaults.removeObjectForKey(key)
            }
        }
    }
}
