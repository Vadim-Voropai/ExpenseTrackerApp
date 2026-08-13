package com.vvv.openexpensetracker.data.source.local

import platform.Foundation.*

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
        val fileUrl = documentUrl.URLByAppendingPathComponent("expenses.json") ?: return

        val nsString = content as NSString
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return
        data.writeToURL(fileUrl, atomically = true)
    }

    override fun loadExpensesFile(): String? {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentUrl = urls.firstOrNull() as? NSURL ?: return null
        val fileUrl = documentUrl.URLByAppendingPathComponent("expenses.json") ?: return null

        if (!fileManager.fileExistsAtPath(fileUrl.path ?: "")) return null

        val data = NSData.dataWithContentsOfURL(fileUrl) ?: return null
        val nsString = NSString.create(data = data, encoding = NSUTF8StringEncoding)
        return nsString as String?
    }
}
