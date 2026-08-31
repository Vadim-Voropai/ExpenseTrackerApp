package com.vvv.openexpensetracker.data.repository

import com.vvv.openexpensetracker.core.Constants
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.data.source.remote.GoogleDriveApi
import com.vvv.openexpensetracker.domain.model.Expense
import com.vvv.openexpensetracker.domain.repository.GoogleDriveRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class GoogleDriveRepositoryImpl(
    private val api: GoogleDriveApi,
    private val localStorage: LocalStorage
) : GoogleDriveRepository {

    override suspend fun findExpensesFile(): String? {
        val cachedFileId = localStorage.getString(Constants.KEY_FILE_ID)
        if (cachedFileId != null) return cachedFileId

        val folderId = api.getAppFolder(createIfMissing = false)
        val fileId = api.findExpensesFile(folderId)
        
        if (fileId != null) {
            localStorage.saveString(Constants.KEY_FILE_ID, fileId)
            if (folderId != null) {
                localStorage.saveString(Constants.KEY_FOLDER_ID, folderId)
            }
        }
        return fileId
    }

    override suspend fun downloadExpenses(fileId: String): List<Expense>? {
        return try {
            api.downloadExpenses(fileId)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                clearCache()
            }
            throw e
        }
    }

    override suspend fun createExpensesFile(): String? {
        val folderId = api.getAppFolder(createIfMissing = true) ?: return null
        val fileId = api.createExpensesFile(folderId)
        
        if (fileId != null) {
            localStorage.saveString(Constants.KEY_FILE_ID, fileId)
            localStorage.saveString(Constants.KEY_FOLDER_ID, folderId)
        }
        return fileId
    }

    override suspend fun updateExpensesFile(fileId: String, expenses: List<Expense>): Boolean {
        return try {
            api.updateExpensesFile(fileId, expenses)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                clearCache()
            }
            throw e
        }
    }

    override fun clearCache() {
        localStorage.saveString(Constants.KEY_FILE_ID, "")
        localStorage.saveString(Constants.KEY_FOLDER_ID, "")
    }
}
