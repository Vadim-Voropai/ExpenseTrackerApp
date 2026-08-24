package com.vvv.openexpensetracker.data.repository

import com.llamatik.library.platform.LlamaBridge
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.domain.repository.LlmRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.HashingSink
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class LlmRepositoryImpl(
    private val httpClient: HttpClient,
    private val localStorage: LocalStorage,
    private val preferencesRepository: PreferencesRepository
) : LlmRepository {

    private val modelFileName = "tinyllama-1.1b.gguf"
    private val modelUrl =
        "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"

    // Expected SHA-256 hash for tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf
    private val expectedModelHash = "9fecc3b3cd76bba89d504f29b616eedf7da85b96540e490ca5824d3f7d2776a0"

    private val modelPath: String
        get() = "${localStorage.getFilesDir()}/$modelFileName"

    private val fs: FileSystem
        get() = localStorage.fileSystem

    override fun isModelDownloadedFlow(): Flow<Boolean> = preferencesRepository.isLlmDownloaded

    override suspend fun downloadModel(): Flow<Float> = channelFlow {
        val statement = httpClient.prepareGet(modelUrl) {
            onDownload { bytesSentTotal, contentLength ->
                if (contentLength != null && (contentLength > 0)) {
                    send(bytesSentTotal.toFloat() / contentLength)
                }
            }
        }

        var calculatedHash = ""

        try {
            statement.execute { response ->
                val channel = response.bodyAsChannel()
                val path = modelPath.toPath()

                val hashingSink = HashingSink.sha256(fs.sink(path))
                try {
                    hashingSink.buffer().use { sink ->
                        val buffer = ByteArray(8192)
                        while (!channel.isClosedForRead) {
                            val read = channel.readAvailable(buffer)
                            if (read == -1) break
                            sink.write(buffer, 0, read)
                        }
                        sink.flush()
                    }
                    calculatedHash = hashingSink.hash.hex()
                } finally {
                    hashingSink.close()
                }
            }

            // Ensure UI reaches 100% before starting integrity check
            send(1.0f)

            if (calculatedHash.equals(expectedModelHash, ignoreCase = true)) {
                preferencesRepository.setLlmDownloaded(true)
            } else {
                val actualHash = calculatedHash.ifEmpty { "EMPTY" }
                // Integrity check failed - delete corrupted file
                try {
                    fs.delete(modelPath.toPath())
                } catch (_: Exception) {}
                send(0f)
                preferencesRepository.setLlmDownloaded(false)
                throw Exception("Integrity mismatch.\nExpected: $expectedModelHash\nActual: $actualHash")
            }
        } catch (e: Exception) {
            // Cleanup on any error during download/hashing
            try {
                fs.delete(modelPath.toPath())
            } catch (_: Exception) {}
            send(0f)
            preferencesRepository.setLlmDownloaded(false)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun initialize(): Boolean {
        return try {
            LlamaBridge.initGenerateModel(modelPath)
            LlamaBridge.updateGenerateParams(
                temperature = 0.1f,
                maxTokens = 256,
                topP = 0.95f,
                topK = 40,
                repeatPenalty = 1.1f,
                contextLength = 2048,
                numThreads = 4,
                useMmap = true,
                flashAttention = false,
                batchSize = 512,
                gpuLayers = 0
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getModelName(): String = "TinyLlama 1.1B"

    override fun deleteModel(): Boolean {
        return try {
            LlamaBridge.shutdown()
            fs.delete(modelPath.toPath())
            preferencesRepository.setLlmDownloaded(false)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun isReceipt(text: String): Boolean {
        val prompt = """
            <|system|>
            You are a receipt analyzer. Answer only 'Yes' or 'No'.
            Is the following text from a POS receipt?
            </s>
            <|user|>
            ${text.take(1000)}
            </s>
            <|assistant|>
        """.trimIndent()

        val response = LlamaBridge.generate(prompt).trim()
        return response.contains("Yes", ignoreCase = true)
    }

    override suspend fun extractReceiptData(text: String): ParsedReceipt? {
        val prompt = """
            <|system|>
            Extract receipt data into JSON.
            Rules:
            1. Output ONLY JSON.
            2. Fields: "vendor", "date", "total".
            3. Date format: YYYY-MM-DD.
            </s>
            <|user|>
            ${text.take(2000)}
            </s>
            <|assistant|>
        """.trimIndent()

        return try {
            val response = LlamaBridge.generate(prompt)
            val jsonStart = response.indexOf('{')
            val jsonEnd = response.lastIndexOf('}') + 1
            if ((jsonStart >= 0) && (jsonEnd > jsonStart)) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                val json = Json { ignoreUnknownKeys = true }
                val map = json.decodeFromString<Map<String, String?>>(jsonStr)

                ParsedReceipt(
                    amount = map["total"]?.toDoubleOrNull(),
                    date = map["date"]?.let { parseDate(it) },
                    merchant = map["vendor"]
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDate(dateStr: String): Long? {
        return try {
            val parts = dateStr.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            LocalDate(year, month, day)
                .atTime(12, 0)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds()
        } catch (_: Exception) {
            null
        }
    }
}
