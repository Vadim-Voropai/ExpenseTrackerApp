package com.vvv.openexpensetracker.data.source.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GoogleDriveApi(private val client: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    // Searches for the "expenses.json" file and returns its file ID if found
    suspend fun findExpensesFile(accessToken: String): String? {
        return try {
            val response = client.get("https://www.googleapis.com/drive/v3/files") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("q", "name='expenses.json' and trashed=false")
                parameter("fields", "files(id,name)")
            }
            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                val jsonObject = json.parseToJsonElement(body).jsonObject
                val files = jsonObject["files"]?.jsonArray
                if (files != null && files.isNotEmpty()) {
                    files[0].jsonObject["id"]?.jsonPrimitive?.content
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Downloads the content of a specific file
    suspend fun downloadExpensesFile(accessToken: String, fileId: String): String? {
        return try {
            val response = client.get("https://www.googleapis.com/drive/v3/files/$fileId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("alt", "media")
            }
            if (response.status == HttpStatusCode.OK) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Creates an empty "expenses.json" metadata entry and returns its file ID
    suspend fun createExpensesFile(accessToken: String): String? {
        return try {
            val response = client.post("https://www.googleapis.com/drive/v3/files") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, "application/json")
                setBody("""{"name": "expenses.json"}""")
            }
            if (response.status == HttpStatusCode.OK) {
                val body = response.bodyAsText()
                val jsonObject = json.parseToJsonElement(body).jsonObject
                jsonObject["id"]?.jsonPrimitive?.content
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Updates the content of "expenses.json"
    suspend fun updateExpensesFile(accessToken: String, fileId: String, content: String): Boolean {
        return try {
            val response = client.patch("https://www.googleapis.com/upload/drive/v3/files/$fileId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, "application/json")
                parameter("uploadType", "media")
                setBody(content)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
