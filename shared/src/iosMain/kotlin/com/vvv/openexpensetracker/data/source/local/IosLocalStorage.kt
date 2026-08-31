package com.vvv.openexpensetracker.data.source.local

import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL

class IosLocalStorage : LocalStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun getFilesDir(): String {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentUrl = urls.firstOrNull() as? NSURL
        return documentUrl?.path ?: ""
    }

    override val fileSystem: okio.FileSystem = okio.FileSystem.SYSTEM
}
