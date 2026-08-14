package com.vvv.openexpensetracker.data.repository

import com.vvv.openexpensetracker.data.source.remote.GoogleDriveApi
import com.vvv.openexpensetracker.domain.repository.GoogleDriveRepository

class GoogleDriveRepositoryImpl(
    private val api: GoogleDriveApi
) : GoogleDriveRepository {

    override suspend fun findExpensesFile(): String? {
        return api.findExpensesFile()
    }

    override suspend fun downloadExpensesFile(fileId: String): String? {
        return api.downloadExpensesFile(fileId)
    }

    override suspend fun createExpensesFile(): String? {
        return api.createExpensesFile()
    }

    override suspend fun updateExpensesFile(fileId: String, content: String): Boolean {
        return api.updateExpensesFile(fileId, content)
    }
}
