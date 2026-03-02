package com.github.darleywey.committranslator.integration

import com.github.darleywey.committranslator.services.TranslationService
import com.github.darleywey.committranslator.settings.CommitTranslatorSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assume.assumeTrue
import org.junit.Assert.*

/**
 * Multi-provider, multi-model integration tests for TranslationService
 *
 * This test suite validates the translateToEnglish method across multiple API providers
 * and their respective models to ensure compatibility and reliability.
 *
 * ## Configuration
 *
 * Each provider is configured using environment variables with the pattern:
 * - TEST_{PROVIDER}_API_KEY: API key for the provider
 * - TEST_{PROVIDER}_API_URL: Base URL for the provider's API
 * - TEST_{PROVIDER}_MODELS: Comma-separated list of models to test
 *
 * ### Supported Providers
 *
 * **OpenAI:**
 * ```bash
 * export TEST_OPENAI_API_KEY="sk-your-key"
 * export TEST_OPENAI_API_URL="https://api.openai.com/v1/chat/completions"
 * export TEST_OPENAI_MODELS="gpt-4o-mini,gpt-4o,gpt-4-turbo"
 * ```
 *
 * **DeepSeek:**
 * ```bash
 * export TEST_DEEPSEEK_API_KEY="your-key"
 * export TEST_DEEPSEEK_API_URL="https://api.deepseek.com/v1/chat/completions"
 * export TEST_DEEPSEEK_MODELS="deepseek-chat,deepseek-coder"
 * ```
 *
 * **Azure OpenAI:**
 * ```bash
 * export TEST_AZURE_API_KEY="your-key"
 * export TEST_AZURE_API_URL="https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2024-02-15"
 * export TEST_AZURE_MODELS="gpt-4,gpt-35-turbo"
 * ```
 *
 * **Ollama (Local):**
 * ```bash
 * export TEST_OLLAMA_API_KEY="dummy"
 * export TEST_OLLAMA_API_URL="http://localhost:11434/v1/chat/completions"
 * export TEST_OLLAMA_MODELS="llama2,mistral"
 * ```
 *
 * ## Running Tests
 *
 * ```bash
 * # Run all configured providers
 * ./gradlew test --tests MultiProviderIntegrationTest
 *
 * # Run specific provider tests
 * ./gradlew test --tests 'MultiProviderIntegrationTest.test*OpenAI*'
 * ```
 *
 * Tests will automatically skip providers that don't have API keys configured.
 */
class MultiProviderIntegrationTest : BasePlatformTestCase() {

    private lateinit var settings: CommitTranslatorSettings
    private lateinit var service: TranslationService

    // Provider configuration data class
    data class ProviderConfig(
        val name: String,
        val apiKey: String?,
        val apiUrl: String,
        val models: List<String>,
        val enabled: Boolean = apiKey != null && apiKey.isNotBlank()
    )

    // Load provider configurations from environment variables
    private val providers: List<ProviderConfig> by lazy {
        listOf(
            ProviderConfig(
                name = "OpenAI",
                apiKey = System.getenv("TEST_OPENAI_API_KEY"),
                apiUrl = System.getenv("TEST_OPENAI_API_URL")
                    ?: "https://api.openai.com/v1/chat/completions",
                models = System.getenv("TEST_OPENAI_MODELS")?.split(",")?.map { it.trim() }
                    ?: listOf("gpt-4o-mini")
            ),
            ProviderConfig(
                name = "DeepSeek",
                apiKey = System.getenv("TEST_DEEPSEEK_API_KEY"),
                apiUrl = System.getenv("TEST_DEEPSEEK_API_URL")
                    ?: "https://api.deepseek.com/v1/chat/completions",
                models = System.getenv("TEST_DEEPSEEK_MODELS")?.split(",")?.map { it.trim() }
                    ?: listOf("deepseek-chat")
            ),
            ProviderConfig(
                name = "Azure",
                apiKey = System.getenv("TEST_AZURE_API_KEY"),
                apiUrl = System.getenv("TEST_AZURE_API_URL")
                    ?: "",  // No default - must be configured
                models = System.getenv("TEST_AZURE_MODELS")?.split(",")?.map { it.trim() }
                    ?: listOf("gpt-4")
            ),
            ProviderConfig(
                name = "Ollama",
                apiKey = System.getenv("TEST_OLLAMA_API_KEY"),
                apiUrl = System.getenv("TEST_OLLAMA_API_URL")
                    ?: "http://localhost:11434/v1/chat/completions",
                models = System.getenv("TEST_OLLAMA_MODELS")?.split(",")?.map { it.trim() }
                    ?: listOf("llama2")
            ),
            ProviderConfig(
                name = "Custom",
                apiKey = System.getenv("TEST_CUSTOM_API_KEY"),
                apiUrl = System.getenv("TEST_CUSTOM_API_URL") ?: "",
                models = System.getenv("TEST_CUSTOM_MODELS")?.split(",")?.map { it.trim() }
                    ?: listOf("default")
            )
        ).filter { it.enabled && it.apiUrl.isNotBlank() }
    }

    override fun setUp() {
        super.setUp()
        settings = CommitTranslatorSettings.getInstance()
        service = TranslationService.getInstance()
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
    // OpenAI Provider Tests
    // ============================================================================

    fun `test OpenAI provider with all configured models`() {
        val provider = providers.find { it.name == "OpenAI" }
        assumeTrue(
            "OpenAI tests skipped: TEST_OPENAI_API_KEY not configured",
            provider != null
        )

        testProviderWithAllModels(provider!!)
    }

    fun `test OpenAI gpt-4o-mini model translation`() {
        val provider = providers.find { it.name == "OpenAI" }
        assumeTrue("OpenAI tests skipped: TEST_OPENAI_API_KEY not configured", provider != null)

        testProviderModelTranslation(provider!!, "gpt-4o-mini", "修复：更新配置文件")
    }

    fun `test OpenAI gpt-4o model translation`() {
        val provider = providers.find { it.name == "OpenAI" }
        assumeTrue("OpenAI tests skipped: TEST_OPENAI_API_KEY not configured", provider != null)

        val hasGpt4o = provider!!.models.any { it.contains("gpt-4o") && !it.contains("mini") }
        assumeTrue("OpenAI gpt-4o not configured in TEST_OPENAI_MODELS", hasGpt4o)

        val model = provider.models.find { it.contains("gpt-4o") && !it.contains("mini") }!!
        testProviderModelTranslation(provider, model, "功能：添加新特性")
    }

    // ============================================================================
    // DeepSeek Provider Tests
    // ============================================================================

    fun `test DeepSeek provider with all configured models`() {
        val provider = providers.find { it.name == "DeepSeek" }
        assumeTrue(
            "DeepSeek tests skipped: TEST_DEEPSEEK_API_KEY not configured",
            provider != null
        )

        testProviderWithAllModels(provider!!)
    }

    fun `test DeepSeek deepseek-chat model translation`() {
        val provider = providers.find { it.name == "DeepSeek" }
        assumeTrue("DeepSeek tests skipped: TEST_DEEPSEEK_API_KEY not configured", provider != null)

        testProviderModelTranslation(provider!!, "deepseek-chat", "测试：验证翻译功能")
    }

    // ============================================================================
    // Azure OpenAI Provider Tests
    // ============================================================================

    fun `test Azure provider with all configured models`() {
        val provider = providers.find { it.name == "Azure" }
        assumeTrue(
            "Azure tests skipped: TEST_AZURE_API_KEY not configured",
            provider != null
        )

        testProviderWithAllModels(provider!!)
    }

    // ============================================================================
    // Ollama Local LLM Provider Tests
    // ============================================================================

    fun `test Ollama provider with all configured models`() {
        val provider = providers.find { it.name == "Ollama" }
        assumeTrue(
            "Ollama tests skipped: TEST_OLLAMA_API_KEY not configured",
            provider != null
        )

        testProviderWithAllModels(provider!!)
    }

    // ============================================================================
    // Custom Provider Tests
    // ============================================================================

    fun `test Custom provider with all configured models`() {
        val provider = providers.find { it.name == "Custom" }
        assumeTrue(
            "Custom provider tests skipped: TEST_CUSTOM_API_KEY not configured",
            provider != null
        )

        testProviderWithAllModels(provider!!)
    }

    // ============================================================================
    // Cross-Provider Consistency Tests
    // ============================================================================

    fun `test all providers produce valid translations`() {
        assumeTrue(
            "No providers configured for testing",
            providers.isNotEmpty()
        )

        val testText = "修复：解决登录问题"
        val results = mutableMapOf<String, Map<String, String>>()

        providers.forEach { provider ->
            val providerResults = mutableMapOf<String, String>()

            provider.models.take(1).forEach { model ->  // Test first model only for speed
                configureProvider(provider, model)

                val result = service.translateToEnglish(testText)
                assertTrue(
                    "${provider.name}/${model} translation should succeed",
                    result.isSuccess
                )

                val translated = result.getOrNull()
                assertNotNull("${provider.name}/${model} translation should not be null", translated)
                assertFalse(
                    "${provider.name}/${model} translation should not be empty",
                    translated.isNullOrBlank()
                )

                providerResults[model] = translated!!
            }

            results[provider.name] = providerResults
        }

        // Print comparison
        println("\n=== Cross-Provider Translation Comparison ===")
        println("Original: $testText")
        results.forEach { (providerName, modelResults) ->
            modelResults.forEach { (model, translation) ->
                println("$providerName/$model: $translation")
            }
        }
        println("==============================================\n")
    }

    fun `test all providers handle multiline commits`() {
        assumeTrue("No providers configured for testing", providers.isNotEmpty())

        val multilineText = """修复：更新认证流程

        - 添加新的验证步骤
        - 改进错误处理""".trimIndent()

        providers.forEach { provider ->
            provider.models.take(1).forEach { model ->
                configureProvider(provider, model)

                val result = service.translateToEnglish(multilineText)
                assertTrue(
                    "${provider.name}/${model} should handle multiline text",
                    result.isSuccess
                )

                println("${provider.name}/${model} multiline result:")
                println(result.getOrNull())
                println("---")
            }
        }
    }

    // ============================================================================
    // Performance Comparison Tests
    // ============================================================================

    fun `test performance across all providers`() {
        assumeTrue("No providers configured for testing", providers.isNotEmpty())

        val testText = "优化：提升性能"
        val performanceResults = mutableMapOf<String, Long>()

        providers.forEach { provider ->
            provider.models.take(1).forEach { model ->
                configureProvider(provider, model)

                val startTime = System.currentTimeMillis()
                val result = service.translateToEnglish(testText)
                val duration = System.currentTimeMillis() - startTime

                assertTrue("${provider.name}/${model} should succeed", result.isSuccess)
                assertTrue(
                    "${provider.name}/${model} should complete in reasonable time (< 30s)",
                    duration < 30000
                )

                performanceResults["${provider.name}/${model}"] = duration
            }
        }

        println("\n=== Performance Comparison ===")
        performanceResults.entries.sortedBy { it.value }.forEach { (key, duration) ->
            println("$key: ${duration}ms")
        }
        println("==============================\n")
    }

    // ============================================================================
    // Model Parameter Compatibility Tests
    // ============================================================================

    fun `test max_tokens vs max_completion_tokens compatibility`() {
        assumeTrue("No providers configured for testing", providers.isNotEmpty())

        val testText = "测试"

        providers.forEach { provider ->
            provider.models.forEach { model ->
                configureProvider(provider, model)

                val result = service.translateToEnglish(testText)

                // Should not fail due to parameter incompatibility
                if (result.isFailure) {
                    val errorMsg = result.exceptionOrNull()?.message ?: ""
                    assertFalse(
                        "${provider.name}/${model} should not have max_tokens parameter error",
                        errorMsg.contains("max_tokens") && errorMsg.contains("not supported")
                    )
                }
            }
        }
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    fun `test all providers return detailed error messages`() {
        assumeTrue("No providers configured for testing", providers.isNotEmpty())

        providers.forEach { provider ->
            val model = provider.models.first()

            // Test with invalid API key
            settings.apiKey = "invalid-key-12345"
            settings.apiUrl = provider.apiUrl
            settings.model = model

            val result = service.translateToEnglish("测试")

            assertTrue("${provider.name}/${model} should fail with invalid key", result.isFailure)

            val errorMsg = result.exceptionOrNull()?.message ?: ""
            assertTrue(
                "${provider.name}/${model} should include error details in message",
                errorMsg.contains("401") ||
                errorMsg.contains("403") ||
                errorMsg.contains("Unauthorized") ||
                errorMsg.contains("API request failed")
            )

            println("${provider.name}/${model} error message: $errorMsg")
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Test a provider with all its configured models
     */
    private fun testProviderWithAllModels(provider: ProviderConfig) {
        println("\n=== Testing ${provider.name} Provider ===")
        println("API URL: ${provider.apiUrl}")
        println("Models: ${provider.models.joinToString(", ")}")

        val testCases = listOf(
            "修复：更新配置" to "Chinese commit",
            "機能追加" to "Japanese commit",
            "버그수정" to "Korean commit",
            "fix: update config" to "English commit"
        )

        provider.models.forEach { model ->
            println("\nTesting model: $model")

            testCases.forEach { (text, description) ->
                configureProvider(provider, model)

                val result = service.translateToEnglish(text)
                assertTrue(
                    "${provider.name}/${model} should translate $description",
                    result.isSuccess
                )

                val translated = result.getOrNull()
                assertNotNull(translated)
                assertFalse(translated.isNullOrBlank())

                println("  $description: $text -> $translated")
            }
        }

        println("=== ${provider.name} Provider Tests Complete ===\n")
    }

    /**
     * Test a specific provider/model combination with a translation
     */
    private fun testProviderModelTranslation(
        provider: ProviderConfig,
        model: String,
        text: String
    ) {
        configureProvider(provider, model)

        val result = service.translateToEnglish(text)

        assertTrue(
            "${provider.name}/${model} translation should succeed",
            result.isSuccess
        )

        val translated = result.getOrNull()
        assertNotNull("Translation should not be null", translated)
        assertFalse("Translation should not be empty", translated.isNullOrBlank())

        println("${provider.name}/${model}:")
        println("  Original: $text")
        println("  Translated: $translated")
    }

    /**
     * Configure the translation service for a specific provider and model
     */
    private fun configureProvider(provider: ProviderConfig, model: String) {
        settings.apiKey = provider.apiKey!!
        settings.apiUrl = provider.apiUrl
        settings.model = model
    }

    /**
     * Print a summary of configured providers
     */
    fun `test show configured providers summary`() {
        println("\n=== Configured Providers Summary ===")
        if (providers.isEmpty()) {
            println("No providers configured. Set environment variables to enable testing:")
            println("  - TEST_OPENAI_API_KEY")
            println("  - TEST_DEEPSEEK_API_KEY")
            println("  - TEST_AZURE_API_KEY")
            println("  - TEST_OLLAMA_API_KEY")
            println("  - TEST_CUSTOM_API_KEY")
        } else {
            providers.forEach { provider ->
                println("\n${provider.name}:")
                println("  URL: ${provider.apiUrl}")
                println("  Models: ${provider.models.joinToString(", ")}")
                println("  Status: ✓ Enabled")
            }
        }
        println("\n====================================\n")
    }
}
