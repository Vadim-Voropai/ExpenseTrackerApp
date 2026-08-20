package com.vvv.openexpensetracker.data.source.remote

import com.vvv.openexpensetracker.core.Constants
import com.vvv.openexpensetracker.core.network.get
import com.vvv.openexpensetracker.core.network.patch
import com.vvv.openexpensetracker.core.network.post
import com.vvv.openexpensetracker.data.model.remote.DriveFile
import com.vvv.openexpensetracker.data.model.remote.DriveFileListResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class UnauthorizedException : Exception("Unauthorized access - token may be expired")

class GoogleDriveApi(private val client: HttpClient) {

    private fun HttpRequestBuilder.addDriveParams(includeItems: Boolean = false) {
        parameter("supportsAllDrives", "true")
        if (includeItems) {
            parameter("includeItemsFromAllDrives", "true")
        }
    }

    private fun Exception.handleUnauthorized() {
        if ((this is ClientRequestException) && response.status == HttpStatusCode.Unauthorized) {
            throw UnauthorizedException()
        }
    }

    suspend fun getAppFolder(createIfMissing: Boolean): String? {
        return try {
            // 1. Search for the folder globally (including shared and Shared Drives)
            val response: DriveFileListResponse = client.get(Constants.GOOGLE_DRIVE_FILES_URL) {
                addDriveParams(includeItems = true)
                parameter("q", "name='${Constants.EXPENSES_FILE_FOLDER_NAME}' and mimeType='application/vnd.google-apps.folder' and trashed = false")
                parameter("fields", "files(id,name)")
            }

            if (response.files.isNotEmpty()) {
                return response.files[0].id
            }

            if (!createIfMissing) return null

            // 2. Create the folder if not found and requested
            val createResponse: DriveFile = client.post(Constants.GOOGLE_DRIVE_FILES_URL) {
                addDriveParams()
                header(HttpHeaders.ContentType, "application/json")
                setBody("""{"name": "${Constants.EXPENSES_FILE_FOLDER_NAME}", "mimeType": "application/vnd.google-apps.folder"}""")
            }
            createResponse.id
        } catch (e: Exception) {
            e.handleUnauthorized()
            e.printStackTrace()
            null
        }
    }

    // Searches for the "expenses.json" file and returns its file ID if found
    suspend fun findExpensesFile(folderId: String? = null): String? {
        return try {
            // 1. If folderId is provided, look specifically in that folder
            if (folderId != null) {
                val response: DriveFileListResponse = client.get(Constants.GOOGLE_DRIVE_FILES_URL) {
                    addDriveParams(includeItems = true)
                    parameter("q", "name='${Constants.EXPENSES_FILE_NAME}' and '$folderId' in parents and trashed = false")
                    parameter("fields", "files(id,name)")
                }

                if (response.files.isNotEmpty()) {
                    return response.files[0].id
                }
            } else {
                // 2. Fallback: Search for any file named "expenses.json" that is shared with the user
                val fallbackResponse: DriveFileListResponse = client.get(Constants.GOOGLE_DRIVE_FILES_URL) {
                    addDriveParams(includeItems = true)
                    parameter("q", "name='${Constants.EXPENSES_FILE_NAME}' and sharedWithMe = true and trashed = false")
                    parameter("fields", "files(id,name)")
                }
                if (fallbackResponse.files.isNotEmpty()) {
                    return fallbackResponse.files[0].id
                }
            }

            null
        } catch (e: Exception) {
            e.handleUnauthorized()
            e.printStackTrace()
            null
        }
    }

    // Downloads the content of a specific file
    suspend fun downloadExpensesFile(fileId: String): String? {
        return try {
            client.get("${Constants.GOOGLE_DRIVE_FILES_URL}/$fileId") {
                addDriveParams(includeItems = true)
                parameter("alt", "media")
            }
        } catch (e: Exception) {
            e.handleUnauthorized()
            throw e
        }
    }

    // Creates an empty "expenses.json" metadata entry and returns its file ID
    suspend fun createExpensesFile(folderId: String): String? {
        return try {
            val createResponse: DriveFile = client.post(Constants.GOOGLE_DRIVE_FILES_URL) {
                addDriveParams()
                header(HttpHeaders.ContentType, "application/json")
                setBody("""{"name": "${Constants.EXPENSES_FILE_NAME}", "parents": ["$folderId"]}""")
            }
            val fileId = createResponse.id

            // Initialize with an empty JSON array to ensure a valid starting state
            updateExpensesFile(fileId, "[]")

            fileId
        } catch (e: Exception) {
            e.handleUnauthorized()
            e.printStackTrace()
            null
        }
    }

    // Updates the content of "expenses.json"
    suspend fun updateExpensesFile(fileId: String, content: String): Boolean {
        return try {
            client.patch<HttpResponse>("${Constants.GOOGLE_DRIVE_UPLOAD_URL}/$fileId") {
                addDriveParams()
                header(HttpHeaders.ContentType, "application/json")
                parameter("uploadType", "media")
                setBody(content)
            }
            true
        } catch (e: Exception) {
            try {
                e.handleUnauthorized()
            } catch (unauthorized: UnauthorizedException) {
                throw unauthorized
            }
            throw e
        }
    }
}
