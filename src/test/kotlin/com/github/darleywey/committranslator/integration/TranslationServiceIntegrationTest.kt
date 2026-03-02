package com.github.darleywey.committranslator.integration

import com.github.darleywey.committranslator.services.TranslationService
import com.github.darleywey.committranslator.settings.CommitTranslatorSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assume.assumeTrue
import org.junit.Assert.*

/**
 * Integration tests for TranslationService with real API calls
 *
 * These tests make actual HTTP requests to OpenAI-compatible APIs.
 * To run these tests, set the following environment variables:
 *
 * - TEST_OPENAI_API_KEY: Your API key for testing
 * - TEST_OPENAI_API_URL (optional): API endpoint URL (defaults to OpenAI)
 * - TEST_OPENAI_MODEL (optional): Model to use (defaults to gpt-4o-mini)
 *
 * Example:
 * ```bash
 * export TEST_OPENAI_API_KEY="sk-your-api-key"
 * ./gradlew test --tests TranslationServiceIntegrationTest
 * ```
 *
 * If the API key is not set, these tests will be skipped.
 */
class TranslationServiceIntegrationTest : BasePlatformTestCase() {

    private lateinit var settings: CommitTranslatorSettings
    private lateinit var service: TranslationService

    // Test configuration from environment variables
    private val testApiKey: String? = System.getenv("TEST_OPENAI_API_KEY")
    private val testApiUrl: String = System.getenv("TEST_OPENAI_API_URL")
        ?: "https://api.openai.com/v1/chat/completions"
    private val testModel: String = System.getenv("TEST_OPENAI_MODEL")
        ?: "gpt-4o-mini"

    override fun setUp() {
        super.setUp()

        // Skip all tests in this class if API key is not configured
        assumeTrue(
            "Integration tests skipped: TEST_OPENAI_API_KEY environment variable not set. " +
            "Set it to run real API integration tests.",
            testApiKey != null && testApiKey.isNotBlank()
        )

        settings = CommitTranslatorSettings.getInstance()
        service = TranslationService.getInstance()

        // Configure with test credentials
        settings.apiKey = testApiKey!!
        settings.apiUrl = testApiUrl
        settings.model = testModel
    }

    override fun tearDown() {
        try {
            // Clean up settings
            settings.apiKey = ""
            settings.apiUrl = "https://api.openai.com/v1/chat/completions"
            settings.model = "gpt-4o-mini"
        } finally {
            super.tearDown()
        }
    }

    // ============================================================================
    // End-to-End Integration Tests with Real API Calls
    // ============================================================================

    fun `test translateToEnglish with Chinese text returns English translation`() {
        // Arrange
        val chineseText = "修复：更新API端点配置"

        // Act
        val result = service.translateToEnglish(chineseText)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)
        assertFalse("Translated text should not be empty", translated.isNullOrBlank())

        // The translation should be different from original (unless already English)
        // and should contain English characters
        println("Original: $chineseText")
        println("Translated: $translated")

        // Verify it looks like a commit message (contains common keywords or format)
        val commonCommitWords = listOf("fix", "update", "add", "remove", "change", "api", "endpoint", "configuration")
        val translatedLower = translated!!.lowercase()
        val containsCommitKeyword = commonCommitWords.any { translatedLower.contains(it) }
        assertTrue("Translation should contain commit-related keywords", containsCommitKeyword)
    }

    fun `test translateToEnglish with Japanese text returns English translation`() {
        // Arrange
        val japaneseText = "機能：新しいユーザー認証システムを追加"

        // Act
        val result = service.translateToEnglish(japaneseText)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)
        assertFalse("Translated text should not be empty", translated.isNullOrBlank())

        println("Original: $japaneseText")
        println("Translated: $translated")

        // Should contain authentication-related words
        val translatedLower = translated!!.lowercase()
        assertTrue(
            "Translation should mention authentication or user",
            translatedLower.contains("auth") ||
            translatedLower.contains("user") ||
            translatedLower.contains("feature") ||
            translatedLower.contains("feat")
        )
    }

    fun `test translateToEnglish with English text returns as-is or improved`() {
        // Arrange
        val englishText = "fix: update API endpoint configuration"

        // Act
        val result = service.translateToEnglish(englishText)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)
        assertFalse("Translated text should not be empty", translated.isNullOrBlank())

        println("Original: $englishText")
        println("Translated: $translated")

        // Should still be a valid commit message
        val translatedLower = translated!!.lowercase()
        assertTrue(
            "Translation should preserve commit message structure",
            translatedLower.contains("fix") ||
            translatedLower.contains("update") ||
            translatedLower.contains("api")
        )
    }

    fun `test translateToEnglish with Korean text returns English translation`() {
        // Arrange
        val koreanText = "버그수정: 로그인 오류 해결"

        // Act
        val result = service.translateToEnglish(koreanText)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)
        assertFalse("Translated text should not be empty", translated.isNullOrBlank())

        println("Original: $koreanText")
        println("Translated: $translated")

        // Should mention bug fix or login
        val translatedLower = translated!!.lowercase()
        assertTrue(
            "Translation should mention bug/fix or login",
            translatedLower.contains("bug") ||
            translatedLower.contains("fix") ||
            translatedLower.contains("login")
        )
    }

    fun `test translateToEnglish with multiline commit message`() {
        // Arrange
        val multilineText = """修复：更新用户认证流程

        - 添加新的验证步骤
        - 改进错误处理
        - 更新相关文档""".trimIndent()

        // Act
        val result = service.translateToEnglish(multilineText)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)
        assertFalse("Translated text should not be empty", translated.isNullOrBlank())

        println("Original:\n$multilineText")
        println("Translated:\n$translated")

        // Should preserve some structure (multiple lines or bullets)
        assertTrue(
            "Translation should have reasonable length",
            translated!!.length > 20
        )
    }

    fun `test translateToEnglish with conventional commit format`() {
        // Arrange
        val conventionalCommit = "feat(auth): 添加OAuth2.0支持"

        // Act
        val result = service.translateToEnglish(conventionalCommit)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)
        assertFalse("Translated text should not be empty", translated.isNullOrBlank())

        println("Original: $conventionalCommit")
        println("Translated: $translated")

        // Should preserve conventional commit format
        val translatedLower = translated!!.lowercase()
        assertTrue(
            "Translation should preserve feat keyword or mention feature",
            translatedLower.contains("feat") ||
            translatedLower.contains("feature") ||
            translatedLower.contains("add")
        )
    }

    fun `test translateToEnglish with special characters and code references`() {
        // Arrange
        val textWithCode = "修复：UserService.kt 中的空指针异常"

        // Act
        val result = service.translateToEnglish(textWithCode)

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)

        val translated = result.getOrNull()
        assertNotNull("Translated text should not be null", translated)

        println("Original: $textWithCode")
        println("Translated: $translated")

        // Should preserve file name
        assertTrue(
            "Translation should preserve code references like UserService.kt",
            translated!!.contains("UserService") || translated.contains("User Service")
        )
    }

    fun `test translateToEnglish performance is reasonable`() {
        // Arrange
        val text = "优化：提升数据库查询性能"
        val maxTimeMs = 30000L // 30 seconds should be plenty for API call

        // Act
        val startTime = System.currentTimeMillis()
        val result = service.translateToEnglish(text)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Assert
        assertTrue("Translation should succeed", result.isSuccess)
        assertTrue(
            "Translation should complete within $maxTimeMs ms (took ${duration}ms)",
            duration < maxTimeMs
        )

        println("Translation completed in ${duration}ms")
    }

    // ============================================================================
    // Tests for Different Models
    // ============================================================================

    fun `test translateToEnglish with gpt-4o-mini model`() {
        // Arrange
        settings.model = "gpt-4o-mini"
        val text = "功能：添加用户配置面板"

        // Act
        val result = service.translateToEnglish(text)

        // Assert
        assertTrue("Translation with gpt-4o-mini should succeed", result.isSuccess)
        assertNotNull("Translation should not be null", result.getOrNull())

        println("Model: gpt-4o-mini")
        println("Translated: ${result.getOrNull()}")
    }

    fun `test translateToEnglish with gpt-5-nano model if available`() {
        // Only run if we're configured to test newer models
        val useGpt5 = System.getenv("TEST_GPT5_AVAILABLE")?.toBoolean() ?: false
        assumeTrue("Skipping gpt-5-nano test (set TEST_GPT5_AVAILABLE=true to enable)", useGpt5)

        // Arrange
        settings.model = "gpt-5-nano"
        val text = "测试：验证新模型支持"

        // Act
        val result = service.translateToEnglish(text)

        // Assert
        assertTrue("Translation with gpt-5-nano should succeed", result.isSuccess)
        assertNotNull("Translation should not be null", result.getOrNull())

        println("Model: gpt-5-nano")
        println("Translated: ${result.getOrNull()}")
    }

    // ============================================================================
    // Error Handling Tests with Real API
    // ============================================================================

    fun `test translateToEnglish with invalid API key returns detailed error`() {
        // Arrange
        settings.apiKey = "invalid-test-key-12345"
        val text = "测试消息"

        // Act
        val result = service.translateToEnglish(text)

        // Assert
        assertTrue("Translation should fail with invalid key", result.isFailure)

        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)

        val errorMessage = exception!!.message ?: ""
        println("Error message: $errorMessage")

        // Should include full error details (as per recent fix)
        assertTrue(
            "Error message should contain API error details",
            errorMessage.contains("401") ||
            errorMessage.contains("403") ||
            errorMessage.contains("Unauthorized") ||
            errorMessage.contains("invalid") ||
            errorMessage.contains("API request failed")
        )
    }

    fun `test translateToEnglish with invalid API URL returns error`() {
        // Arrange
        settings.apiUrl = "https://invalid-api-endpoint-test.example.com/v1/chat/completions"
        val text = "测试消息"

        // Act
        val result = service.translateToEnglish(text)

        // Assert
        assertTrue("Translation should fail with invalid URL", result.isFailure)

        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)

        println("Error: ${exception!!.message}")
    }

    // ============================================================================
    // Stress Tests
    // ============================================================================

    fun `test translateToEnglish with very long text`() {
        // Arrange
        val longText = "修复：" + "解决用户报告的问题。".repeat(50)

        // Act
        val result = service.translateToEnglish(longText)

        // Assert
        assertTrue("Translation should succeed even with long text", result.isSuccess)
        assertNotNull("Translation should not be null", result.getOrNull())

        println("Long text length: ${longText.length}")
        println("Translated length: ${result.getOrNull()?.length}")
    }

    fun `test translateToEnglish with empty text after trim`() {
        // Arrange
        val text = "   "

        // Act
        val result = service.translateToEnglish(text)

        // Assert
        // This might succeed or fail depending on API behavior
        // Just verify we get a consistent response
        println("Empty text result: ${result.isSuccess}")
        if (result.isSuccess) {
            println("Translation: ${result.getOrNull()}")
        } else {
            println("Error: ${result.exceptionOrNull()?.message}")
        }
    }
}
