package com.vvv.openexpensetracker.data.source.local

import com.vvv.openexpensetracker.core.Constants
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

    override fun saveExpensesFile(content: String) {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentUrl = urls.firstOrNull() as? NSURL ?: return
        val fileUrl = documentUrl.URLByAppendingPathComponent(Constants.EXPENSES_FILE_NAME) ?: return

        val nsString = content as NSString
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return
        data.writeToURL(fileUrl, atomically = true)
    }

    override fun loadExpensesFile(): String? {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentUrl = urls.firstOrNull() as? NSURL ?: return null
        val fileUrl = documentUrl.URLByAppendingPathComponent(Constants.EXPENSES_FILE_NAME) ?: return null

        if (!fileManager.fileExistsAtPath(fileUrl.path ?: "")) return null

        val data = NSData.dataWithContentsOfURL(fileUrl) ?: return null
        val nsString = NSString.create(data = data, encoding = NSUTF8StringEncoding)
        return nsString as String?
    }
}
