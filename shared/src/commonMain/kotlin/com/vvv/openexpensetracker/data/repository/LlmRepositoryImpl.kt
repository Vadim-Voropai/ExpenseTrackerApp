package com.vvv.openexpensetracker.data.repository

import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.LlamaBridge
import com.vvv.openexpensetracker.data.source.local.LocalStorage
import com.vvv.openexpensetracker.domain.model.Category
import com.vvv.openexpensetracker.domain.repository.LlmBenchmarkResult
import com.vvv.openexpensetracker.domain.repository.LlmRepository
import com.vvv.openexpensetracker.domain.repository.PreferencesRepository
import com.vvv.openexpensetracker.domain.util.ParsedReceipt
import com.vvv.openexpensetracker.domain.util.ReceiptParser
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

    private val modelFileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf"
    private val modelUrl =
        "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf"

    // Expected SHA-256 hash for qwen2.5-1.5b-instruct-q4_k_m.gguf
    private val expectedModelHash = "6a1a2eb6d15622bf3c96857206351ba97e1af16c30d7a74ee38970e434e9407e"

    private val modelPath: String
        get() = "${localStorage.getFilesDir()}/$modelFileName"

    private val fs: FileSystem
        get() = localStorage.fileSystem

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isInitialized = false

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
                initialize()
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
        if (!fs.exists(modelPath.toPath())) {
            println("LLM Init failed: Model file not found at $modelPath")
            return false
        }

        return try {
            println("Initializing LLM model from $modelPath...")
            LlamaBridge.initGenerateModel(modelPath)
            LlamaBridge.updateGenerateParams(
                temperature = 0.1f,
                maxTokens = 512,
                topP = 0.95f,
                topK = 40,
                repeatPenalty = 1.1f,
                contextLength = 4096, // Increased context for Qwen 2.5
                numThreads = 8,
                useMmap = true,
                flashAttention = false,
                batchSize = 512,
                gpuLayers = 0
            )
            isInitialized = true
            println("LLM Initialized successfully")
            true
        } catch (e: Exception) {
            println("LLM Initialization failed: ${e.message}")
            e.printStackTrace()
            isInitialized = false
            false
        }
    }

    override fun getModelName(): String = "Qwen 2.5 1.5B"

    override fun deleteModel(): Boolean {
        return try {
            LlamaBridge.shutdown()
            isInitialized = false
            fs.delete(modelPath.toPath())
            preferencesRepository.setLlmDownloaded(false)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun normalizeReceiptData(text: String): String {
        val trimmedText = ReceiptParser.trimAfterTotal(text)
        // Advanced prompt for Qwen 2.5 to repair and normalize OCR text
        val prompt = """
            <|im_start|>system
            You are an expert POS receipt data cleaner.
            Your task is to repair "broken" OCR text from physical receipts.
            Fix character substitutions (e.g., 'S' for '5', 'O' for '0', '|' for '1'), repair fragmented vendor names, and normalize dates (use current year if missing).
            Rules:
            1. Output ONLY the repaired and structured string.
            2. If a word is beyond repair, remove it.
            <|im_end|>
            <|im_start|>user
            Input: "$trimmedText"
            <|im_end|>
            <|im_start|>assistant
        """.trimIndent()
        
        return makeLLMRequest(prompt) ?: text
    }

    override suspend fun extractReceiptData(text: String): ParsedReceipt? =
        withContext(Dispatchers.IO) {
            val categories = Category.list
            val prompt = """
                <|im_start|>system
                You are a POS receipt data extractor. Extract data from the user input into JSON.
                Rules:
                1. Output ONLY a raw JSON object.
                2. Fields: "total" (number), "category" (string).
                4. Use one of these categories: $categories.
                5. Use the exact total amount from the receipt (round up).
                6. If a field is unknown, use null.
                <|im_end|>
                <|im_start|>user
                Input: "$text"
                <|im_end|>
                <|im_start|>assistant
                {
            """.trimIndent()

            try {
                val response = makeLLMRequest(prompt) ?: return@withContext null
                // Robust extraction: isolate the JSON block
                val jsonStart = response.indexOf('{')
                val jsonEnd = response.lastIndexOf('}') + 1
                val jsonStr = if (jsonStart in 0..<jsonEnd) {
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
                    category = jsonObject["category"].asString(),
                )
            } catch (e: Exception) {
                println("Error parsing LLM response: ${e.message}")
                null
            }
        }

    private suspend fun makeLLMRequest(prompt: String): String? =
        withContext(Dispatchers.IO) {
            if (!isInitialized) {
                println("LLM not initialized. Attempting JIT initialization...")
                if (!initialize()) {
                    println("JIT Initialization failed.")
                    return@withContext null
                }
            }

            try {
                println("Receipt prompt (ChatML): $prompt")

                var tokenCount = 0
                var firstTokenTime: kotlin.time.TimeMark? = null
                val startTime = TimeSource.Monotonic.markNow()
                // If the prompt already contains the opening '{', we start with it
                val resultBuilder = StringBuilder(if (prompt.endsWith("{")) "{" else "")

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

                println("LLM Request took: ${totalDuration.toDouble(DurationUnit.SECONDS)}s ($tps tps)")
                println("Receipt full JSON: $response")
                return@withContext response
            } catch (e: Exception) {
                println("Error parsing LLM response: ${e.message}")
                return@withContext null
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
