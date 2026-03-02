package com.github.darleywey.committranslator.services

import com.github.darleywey.committranslator.settings.CommitTranslatorSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service(Service.Level.APP)
class TranslationService {

    private val logger = Logger.getInstance(TranslationService::class.java)
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Serializable
    data class ChatMessage(
        val role: String,
        val content: String
    )

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>
    )

    @Serializable
    data class ChatChoice(
        val message: ChatMessage
    )

    @Serializable
    data class ChatResponse(
        val choices: List<ChatChoice>
    )

    fun testConnection(apiUrl: String, apiKey: String, model: String, input: String? = null): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("API Key is not configured"))
        }

        if (apiUrl.isBlank()) {
            return Result.failure(IllegalStateException("API URL is not configured"))
        }

        val systemPrompt = """You are a professional translator specializing in git commit messages.
Your task is to translate the given commit message to English.
Rules:
1. Keep the translation concise and professional
2. Preserve any technical terms, file names, or code references
3. Follow conventional commit format if the original uses it
4. Only output the translated commit message, nothing else
5. If the input is already in English, return it as is with minor improvements if needed"""

        val request = if (input != null) {
            ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", systemPrompt),
                    ChatMessage("user", input)
                )
            )
        } else {
            ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("user", "Hello! This is a test connection message. Please reply with exactly 'OK'.")
                )
            )
        }

        return try {
            val requestBody = json.encodeToString(request)

            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
                .timeout(Duration.ofSeconds(15)) // Shorter timeout for testing
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                val errorBody = response.body()
                logger.warn("Test connection failed with status ${response.statusCode()}: $errorBody")
                return Result.failure(RuntimeException("Connection failed (${response.statusCode()}):\n$errorBody"))
            }

            if (input != null) {
                val chatResponse = json.decodeFromString<ChatResponse>(response.body())
                val translatedText = chatResponse.choices.firstOrNull()?.message?.content
                    ?: return Result.failure(RuntimeException("No response from API"))
                return Result.success(translatedText.trim())
            }

            Result.success("Connection successful! API is working properly.")
        } catch (e: Exception) {
            logger.warn("Test connection failed", e)
            Result.failure(e)
        }
    }

    fun translateToEnglish(text: String): Result<String> {
        val settings = CommitTranslatorSettings.getInstance()
        
        if (settings.apiKey.isBlank()) {
            return Result.failure(IllegalStateException("API Key is not configured"))
        }
        
        if (settings.apiUrl.isBlank()) {
            return Result.failure(IllegalStateException("API URL is not configured"))
        }

        val systemPrompt = """You are a professional translator specializing in git commit messages.
Your task is to translate the given commit message to English.
Rules:
1. Keep the translation concise and professional
2. Preserve any technical terms, file names, or code references
3. Follow conventional commit format if the original uses it
4. Only output the translated commit message, nothing else
5. If the input is already in English, return it as is with minor improvements if needed"""

        val request = ChatRequest(
            model = settings.model,
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", text)
            )
        )

        return try {
            val requestBody = json.encodeToString(request)
            
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(settings.apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer ${settings.apiKey}")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                val errorBody = response.body()
                logger.warn("API request failed with status ${response.statusCode()}: $errorBody")
                return Result.failure(RuntimeException("API request failed (${response.statusCode()}): $errorBody"))
            }

            val chatResponse = json.decodeFromString<ChatResponse>(response.body())
            val translatedText = chatResponse.choices.firstOrNull()?.message?.content
                ?: return Result.failure(RuntimeException("No response from API"))
            
            Result.success(translatedText.trim())
        } catch (e: Exception) {
            logger.error("Translation failed", e)
            Result.failure(e)
        }
    }

    companion object {
        fun getInstance(): TranslationService {
            return ApplicationManager.getApplication().getService(TranslationService::class.java)
        }
    }
}
