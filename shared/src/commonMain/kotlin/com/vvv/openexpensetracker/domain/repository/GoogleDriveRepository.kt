package com.vvv.openexpensetracker.domain.repository

interface GoogleDriveRepository {
    suspend fun findExpensesFile(): String?
    suspend fun downloadExpensesFile(fileId: String): String?
    suspend fun createExpensesFile(): String?
    suspend fun updateExpensesFile(fileId: String, content: String): Boolean
}
