# 测试 API Key 配置指南

## 问题解答：在哪里配置测试用的 API Key？

本项目的测试**不需要真实的 API Key**。当前的测试主要验证配置逻辑和代码正确性，不会实际调用 OpenAI API。

## 测试中的 API Key 配置方式

### 方法一：在测试代码中直接设置（推荐）

测试使用 `CommitTranslatorSettings.getInstance()` 来配置 API Key：

```kotlin
// 位置: src/test/kotlin/.../TranslationServiceTest.kt

override fun setUp() {
    super.setUp()
    settings = CommitTranslatorSettings.getInstance()
    service = TranslationService.getInstance()
}

// 在测试方法中配置 API Key
fun `test example with API key`() {
    // Arrange - 配置测试用的 API Key
    settings.apiKey = "test-key-12345"
    settings.apiUrl = "https://api.openai.com/v1/chat/completions"
    settings.model = "gpt-4o-mini"

    // Act - 执行测试
    val result = service.translateToEnglish("测试消息")

    // Assert - 验证结果
    // ...
}
```

### 方法二：使用空字符串测试验证逻辑

大多数测试使用空字符串或简单的测试值：

```kotlin
fun `test translateToEnglish fails when API key is not configured`() {
    // 测试 API Key 为空的情况
    settings.apiKey = ""
    settings.apiUrl = "https://api.openai.com/v1/chat/completions"

    val result = service.translateToEnglish("测试消息")

    // 验证应该返回失败
    assertTrue(result.isFailure)
    assertEquals("API Key is not configured", exception?.message)
}
```

## 当前测试的类型

### ✅ 单元测试（不需要真实 API Key）

当前的测试主要包括：

1. **配置验证测试**
   - 验证 API Key 为空时的错误处理
   - 验证 API URL 为空时的错误处理
   - 验证配置是否正确保存和读取

2. **模型检测测试**
   - 验证 gpt-5 系列模型识别
   - 验证 o1、o3 系列模型识别
   - 验证参数选择逻辑（max_tokens vs max_completion_tokens）

3. **数据序列化测试**
   - 验证请求 JSON 序列化
   - 验证响应 JSON 反序列化

这些测试**不会发起真实的 HTTP 请求**，因此不需要真实的 API Key。

### ❌ 集成测试（需要真实 API Key，目前未实现）

如果未来需要添加集成测试来验证与真实 API 的交互，可以考虑以下配置方式：

#### 选项 1：环境变量（推荐用于 CI/CD）

```kotlin
// 未来可能的实现方式
fun `test real API integration`() {
    val apiKey = System.getenv("TEST_OPENAI_API_KEY") ?: skip("No API key provided")
    settings.apiKey = apiKey

    // 测试真实 API 调用...
}
```

在 GitHub Actions 中配置：
```yaml
# .github/workflows/build.yml
- name: Run Integration Tests
  env:
    TEST_OPENAI_API_KEY: ${{ secrets.TEST_OPENAI_API_KEY }}
  run: ./gradlew integrationTest
```

#### 选项 2：本地配置文件（推荐用于本地开发）

```kotlin
// 未来可能的实现方式
// src/test/resources/test.properties
fun loadTestConfig() {
    val props = Properties()
    props.load(this.javaClass.getResourceAsStream("/test.properties"))
    return props.getProperty("test.api.key")
}
```

创建 `src/test/resources/test.properties`（需添加到 .gitignore）：
```properties
test.api.key=sk-your-test-key
test.api.url=https://api.openai.com/v1/chat/completions
test.model=gpt-4o-mini
```

#### 选项 3：Mock HTTP 服务器（最佳实践）

```kotlin
// 使用 MockWebServer 或类似工具模拟 API 响应
fun `test with mock server`() {
    val mockServer = MockWebServer()
    mockServer.enqueue(MockResponse()
        .setBody("""{"choices":[{"message":{"content":"Translated text"}}]}""")
        .setResponseCode(200))

    settings.apiUrl = mockServer.url("/v1/chat/completions").toString()
    settings.apiKey = "mock-key"

    // 测试...
    mockServer.shutdown()
}
```

## 运行测试

### 本地运行（无需配置 API Key）

```bash
# 运行所有测试
./gradlew test

# 运行测试并生成覆盖率报告
./gradlew check

# 查看覆盖率报告
./gradlew koverHtmlReport
open build/reports/kover/html/index.html
```

### GitHub Actions（自动运行）

测试会在以下情况自动运行，**无需配置 API Key**：
- 推送到 main 分支
- 创建或更新 Pull Request

查看测试结果：
1. 访问仓库的 **Actions** 标签页
2. 选择最近的工作流运行
3. 点击 **Test** 作业查看详细结果

## 测试数据清理

测试结束后会自动清理配置：

```kotlin
override fun tearDown() {
    try {
        // 清理测试设置
        settings.apiKey = ""
        settings.apiUrl = "https://api.openai.com/v1/chat/completions"
        settings.model = "gpt-4o-mini"
    } finally {
        super.tearDown()
    }
}
```

## 生产环境的 API Key 配置

**注意**：测试配置与生产环境配置是分开的！

### 生产环境配置方式

在 IntelliJ IDEA 中配置（用于实际使用插件）：

1. 打开 **Settings/Preferences** > **Tools** > **Commit Translator**
2. 配置以下内容：
   - **API URL**: 你的 OpenAI 兼容 API 端点
   - **API Key**: 你的真实 API Key（安全存储在 IDE 的凭证管理器中）
   - **Model**: 使用的模型（如 gpt-4o-mini, gpt-5-nano）

生产环境的 API Key 存储在：
- **Windows**: `%APPDATA%\JetBrains\<product>\<version>\config\`
- **macOS**: `~/Library/Application Support/JetBrains/<product>/`
- **Linux**: `~/.config/JetBrains/<product>/`

通过 IntelliJ 的 `PasswordSafe` 安全存储，不会明文保存。

## 测试配置最佳实践

### ✅ 推荐做法

1. **单元测试**：使用简单的测试字符串（如 `"test-key"`）
2. **配置验证**：测试空值、无效值等边界情况
3. **Mock 测试**：使用 Mock 对象模拟 API 响应
4. **隔离测试**：每个测试独立运行，互不影响

### ❌ 避免做法

1. **不要在代码中硬编码真实的 API Key**
2. **不要提交包含 API Key 的配置文件**
3. **不要在单元测试中调用真实的 API**（耗时、不稳定、消耗配额）
4. **不要将测试 API Key 提交到 Git 仓库**

## 安全注意事项

### 保护 API Key

如果需要在测试中使用真实 API Key：

1. **使用环境变量**
   ```bash
   export TEST_OPENAI_API_KEY="your-key"
   ./gradlew test
   ```

2. **使用 .gitignore**
   ```gitignore
   # 添加到 .gitignore
   src/test/resources/test.properties
   src/test/resources/local.properties
   *.local.properties
   ```

3. **使用 GitHub Secrets**
   - 在仓库设置中添加 Secret
   - 在 workflow 中通过 `${{ secrets.SECRET_NAME }}` 引用
   - Secret 不会出现在日志中

4. **使用专用的测试账号**
   - 为测试创建单独的 API Key
   - 设置较低的使用限额
   - 定期轮换密钥

## 总结

### 当前项目的测试配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| API Key | `"test-key"` 或 `""` | 在测试代码中直接设置 |
| API URL | `"https://api.openai.com/v1/chat/completions"` | 在测试代码中直接设置 |
| Model | `"gpt-4o-mini"` | 在测试代码中直接设置 |
| 环境变量 | 不使用 | 当前测试不需要 |
| 配置文件 | 不存在 | 不需要额外配置文件 |
| 真实 API 调用 | 否 | 仅测试配置逻辑 |

### 快速开始

如果你想添加新的测试：

```kotlin
class MyNewTest : BasePlatformTestCase() {
    private lateinit var settings: CommitTranslatorSettings

    override fun setUp() {
        super.setUp()
        settings = CommitTranslatorSettings.getInstance()
    }

    fun `test my feature`() {
        // 配置测试 API Key
        settings.apiKey = "test-key"
        settings.apiUrl = "https://api.openai.com/v1/chat/completions"
        settings.model = "gpt-4o-mini"

        // 你的测试代码...
    }

    override fun tearDown() {
        try {
            settings.apiKey = ""  // 清理
        } finally {
            super.tearDown()
        }
    }
}
```

## 相关文档

- 📖 [测试文档（中文）](TESTING_CN.md)
- 📖 [Testing Guide (English)](src/test/README.md)
- 📖 [Main README](README.md)

## 问题排查

### Q: 测试运行失败，提示找不到 API Key

**A**: 这是正常的！很多测试就是为了验证"API Key 未配置"的错误处理。检查测试断言，确认是否期望失败。

### Q: 我想测试真实的 API 调用怎么办？

**A**: 当前测试不支持真实 API 调用。如果需要：
1. 考虑使用 Mock HTTP 服务器
2. 或在本地手动测试插件功能（在 IDE 中安装并配置真实 API Key）

### Q: 测试在 CI 中失败了

**A**:
1. 查看 GitHub Actions 日志
2. 确认不是配置问题（当前测试不需要配置）
3. 检查是否是代码逻辑错误
4. 下载测试报告 Artifact 查看详细信息

### Q: 如何本地调试测试？

**A**:
```bash
# 运行单个测试类
./gradlew test --tests TranslationServiceTest

# 运行单个测试方法
./gradlew test --tests 'TranslationServiceTest.test translateToEnglish fails when API key is not configured'

# 显示详细输出
./gradlew test --info
```
