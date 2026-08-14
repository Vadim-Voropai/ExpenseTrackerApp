package com.vvv.openexpensetracker.data.source.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.vvv.openexpensetracker.core.Constants
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class UnauthorizedException : Exception("Unauthorized access - token may be expired")

class GoogleDriveApi(private val client: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun getBaseUrl(): String = ""

    private suspend inline fun <reified T> post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.post("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            if (response.status == HttpStatusCode.Unauthorized) throw UnauthorizedException()
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.get("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            if (response.status == HttpStatusCode.Unauthorized) throw UnauthorizedException()
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> patch(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.patch("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            if (response.status == HttpStatusCode.Unauthorized) throw UnauthorizedException()
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> put(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.put("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            if (response.status == HttpStatusCode.Unauthorized) throw UnauthorizedException()
            throw ClientRequestException(response, "")
        }
    }

    private suspend inline fun <reified T> delete(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.delete("${getBaseUrl()}$urlString", block)
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            if (response.status == HttpStatusCode.Unauthorized) throw UnauthorizedException()
            throw ClientRequestException(response, "")
        }
    }

    private suspend fun getOrCreateFolder(): String? {
        return try {
            // 1. Search for the folder
            val searchBody: String = get(Constants.GOOGLE_DRIVE_FILES_URL) {
                parameter("q", "name='${Constants.EXPENSES_FILE_FOLDER_NAME}' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                parameter("fields", "files(id)")
            }

            val files = json.parseToJsonElement(searchBody).jsonObject["files"]?.jsonArray
            if (files != null && files.isNotEmpty()) {
                return files[0].jsonObject["id"]?.jsonPrimitive?.content
            }

            // 2. Create the folder if not found
            val createBody: String = post(Constants.GOOGLE_DRIVE_FILES_URL) {
                header(HttpHeaders.ContentType, "application/json")
                setBody("""{"name": "${Constants.EXPENSES_FILE_FOLDER_NAME}", "mimeType": "application/vnd.google-apps.folder"}""")
            }
            json.parseToJsonElement(createBody).jsonObject["id"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            if (e is UnauthorizedException) throw e
            e.printStackTrace()
            null
        }
    }

    // Searches for the "expenses.json" file and returns its file ID if found
    suspend fun findExpensesFile(): String? {
        return try {
            val folderId = getOrCreateFolder() ?: return null

            val body: String = get(Constants.GOOGLE_DRIVE_FILES_URL) {
                parameter("q", "name='${Constants.EXPENSES_FILE_NAME}' and '$folderId' in parents and trashed=false")
                parameter("fields", "files(id,name)")
            }

            val jsonObject = json.parseToJsonElement(body).jsonObject
            val files = jsonObject["files"]?.jsonArray
            if (files != null && files.isNotEmpty()) {
                files[0].jsonObject["id"]?.jsonPrimitive?.content
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is UnauthorizedException) throw e
            e.printStackTrace()
            null
        }
    }

    // Downloads the content of a specific file
    suspend fun downloadExpensesFile(fileId: String): String? {
        return try {
            get("${Constants.GOOGLE_DRIVE_FILES_URL}/$fileId") {
                parameter("alt", "media")
            }
        } catch (e: Exception) {
            if (e is UnauthorizedException) throw e
            e.printStackTrace()
            null
        }
    }

    // Creates an empty "expenses.json" metadata entry and returns its file ID
    suspend fun createExpensesFile(): String? {
        return try {
            val folderId = getOrCreateFolder() ?: return null

            val body: String = post(Constants.GOOGLE_DRIVE_FILES_URL) {
                header(HttpHeaders.ContentType, "application/json")
                setBody("""{"name": "${Constants.EXPENSES_FILE_NAME}", "parents": ["$folderId"]}""")
            }
            val jsonObject = json.parseToJsonElement(body).jsonObject
            jsonObject["id"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            if (e is UnauthorizedException) throw e
            e.printStackTrace()
            null
        }
    }

    // Updates the content of "expenses.json"
    suspend fun updateExpensesFile(fileId: String, content: String): Boolean {
        return try {
            patch<HttpResponse>("${Constants.GOOGLE_DRIVE_UPLOAD_URL}/$fileId") {
                header(HttpHeaders.ContentType, "application/json")
                parameter("uploadType", "media")
                setBody(content)
            }
            true
        } catch (e: Exception) {
            if (e is UnauthorizedException) throw e
            e.printStackTrace()
            false
        }
    }
}
