package com.vvv.openexpensetracker.data.repository

import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.LlamaBridge
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.domain.repository.LlmBenchmarkResult
import com.vvv.openexpensetracker.domain.repository.LlmRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import okio.FileSystem
import okio.HashingSource
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

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Self-healing check: Verify actual file presence and integrity on startup in background
        repositoryScope.launch {
            val isVerified = verifyModelIntegrity()
            preferencesRepository.setLlmDownloaded(isVerified)
        }
    }

    private suspend fun verifyModelIntegrity(): Boolean = withContext(Dispatchers.IO) {
        val path = modelPath.toPath()
        if (!fs.exists(path)) return@withContext false

        try {
            val hashingSource = HashingSource.sha256(fs.source(path))
            hashingSource.buffer().use { source ->
                val sink = okio.Buffer()
                while (source.read(sink, 8192L) != -1L) {
                    sink.clear()
                }
            }
            val calculatedHash = hashingSource.hash.hex()
            calculatedHash.equals(expectedModelHash, ignoreCase = true)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun isModelDownloadedFlow(): Flow<Boolean> = preferencesRepository.isLlmDownloaded

    override suspend fun downloadModel(): Flow<Float> = channelFlow {
        val statement = httpClient.prepareGet(modelUrl) {
            onDownload { bytesSentTotal, contentLength ->
                if (contentLength != null && (contentLength > 0)) {
                    send(bytesSentTotal.toFloat() / contentLength)
                }
            }
        }

        try {
            statement.execute { response ->
                val channel = response.bodyAsChannel()
                val path = modelPath.toPath()

                fs.sink(path).buffer().use { sink ->
                    val buffer = ByteArray(8192)
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer)
                        if (read == -1) break
                        sink.write(buffer, 0, read)
                    }
                    sink.flush()
                }
            }

            // Ensure UI reaches 100%
            send(1.0f)

            // Reuse the same integrity check logic
            if (verifyModelIntegrity()) {
                preferencesRepository.setLlmDownloaded(true)
            } else {
                // Integrity check failed - delete corrupted file
                try {
                    fs.delete(modelPath.toPath())
                } catch (_: Exception) {
                }
                send(0f)
                preferencesRepository.setLlmDownloaded(false)
                throw Exception("Model integrity check failed after download.")
            }
        } catch (e: Exception) {
            // Cleanup on any error during download/verification
            try {
                fs.delete(modelPath.toPath())
            } catch (_: Exception) {
            }
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
                numThreads = 8,
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

    override suspend fun extractReceiptData(text: String): ParsedReceipt? =
        withContext(Dispatchers.IO) {
            val cleanedText = text.replace("\n", " ").take(2000)
            val prompt = """
            Input: "$cleanedText"
            Extract the data into JSON with keys "vendor", "date" (YYYY-MM-DD), "total" (number), "category", "items".
            JSON: {
        """.trimIndent()

            try {
                println("Receipt prompt (Anchor): $prompt")

                var tokenCount = 0
                var firstTokenTime: kotlin.time.TimeMark? = null
                val startTime = TimeSource.Monotonic.markNow()
                val resultBuilder = StringBuilder("{")

                val deferred = CompletableDeferred<String>()

                LlamaBridge.generateStream(prompt, object : GenStream {
                    override fun onDelta(text: String) {
                        if (tokenCount == 0) {
                            firstTokenTime = TimeSource.Monotonic.markNow()
                        }
                        tokenCount++
                        resultBuilder.append(text)
                    }

                    override fun onComplete() {
                        deferred.complete(resultBuilder.toString())
                    }

                    override fun onError(message: String) {
                        deferred.completeExceptionally(Exception(message))
                    }
                })

                val response = deferred.await().trim()

                // Performance calculation
                val totalDuration = startTime.elapsedNow()
                val decodeDuration = firstTokenTime?.elapsedNow() ?: totalDuration
                val tps = if (decodeDuration.toDouble(DurationUnit.SECONDS) > 0) {
                    tokenCount.toDouble() / decodeDuration.toDouble(DurationUnit.SECONDS)
                } else 0.0

                // Save metrics
                preferencesRepository.setLlmBenchmarkResult(
                    LlmBenchmarkResult(tps, totalDuration.toLong(DurationUnit.MILLISECONDS))
                )

                println("Receipt full JSON: $response")

                // Robust extraction: isolate the JSON block in case of trailing noise
                val jsonStart = response.indexOf('{')
                val jsonEnd = response.lastIndexOf('}') + 1
                val jsonStr = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    response.substring(jsonStart, jsonEnd)
                } else {
                    response
                }

                val json = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }

                val jsonElement = json.parseToJsonElement(jsonStr)
                val jsonObject = jsonElement as? JsonObject ?: return@withContext null

                fun JsonElement?.asString(): String? =
                    (this as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

                fun JsonElement?.asDouble(): Double? =
                    (this as? JsonPrimitive)?.doubleOrNull?.takeIf { it > 0.0 }

                ParsedReceipt(
                    amount = jsonObject["total"].asDouble(),
                    date = jsonObject["date"].asString()?.let { parseDate(it) },
                    category = jsonObject["category"].asString(),
                    merchant = jsonObject["vendor"].asString(),
                    items = jsonObject["items"].asString(),
                )
            } catch (e: Exception) {
                println("Error parsing LLM response: ${e.message}")
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
