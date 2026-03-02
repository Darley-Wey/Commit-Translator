# 端到端集成测试指南

## 概述

本项目现在包含**真实 API 集成测试**，可以验证 `translateToEnglish` 方法的端到端功能。这些测试会发起真实的 HTTP 请求到 OpenAI 兼容的 API。

## 测试位置

```
src/test/kotlin/com/github/darleywey/committranslator/integration/
└── TranslationServiceIntegrationTest.kt  # 真实 API 集成测试
```

## 配置 API Key

### 方法一：使用环境变量（推荐）

设置以下环境变量：

```bash
# 必需：你的测试 API Key
export TEST_OPENAI_API_KEY="sk-your-api-key-here"

# 可选：自定义 API 端点（默认：OpenAI）
export TEST_OPENAI_API_URL="https://api.openai.com/v1/chat/completions"

# 可选：指定测试模型（默认：gpt-4o-mini）
export TEST_OPENAI_MODEL="gpt-4o-mini"

# 可选：启用 GPT-5 模型测试
export TEST_GPT5_AVAILABLE="true"
```

### 方法二：使用一次性命令

```bash
TEST_OPENAI_API_KEY="sk-your-key" ./gradlew test --tests TranslationServiceIntegrationTest
```

### 方法三：创建本地配置脚本（推荐本地开发）

创建文件 `test-env.sh`（已添加到 .gitignore）：

```bash
#!/bin/bash
export TEST_OPENAI_API_KEY="sk-your-api-key"
export TEST_OPENAI_API_URL="https://api.openai.com/v1/chat/completions"
export TEST_OPENAI_MODEL="gpt-4o-mini"
```

使用：

```bash
source test-env.sh
./gradlew test --tests TranslationServiceIntegrationTest
```

## 运行集成测试

### 运行所有集成测试

```bash
# 设置 API Key
export TEST_OPENAI_API_KEY="sk-your-key"

# 运行集成测试
./gradlew test --tests TranslationServiceIntegrationTest

# 或者查看详细输出
./gradlew test --tests TranslationServiceIntegrationTest --info
```

### 运行单个集成测试

```bash
# 运行特定测试
./gradlew test --tests 'TranslationServiceIntegrationTest.test translateToEnglish with Chinese text returns English translation'

# 使用通配符运行一组测试
./gradlew test --tests 'TranslationServiceIntegrationTest.*Chinese*'
```

### 运行所有测试（单元 + 集成）

```bash
export TEST_OPENAI_API_KEY="sk-your-key"
./gradlew test
```

**注意**：如果没有设置 `TEST_OPENAI_API_KEY`，集成测试会自动跳过，不会失败。

## 测试覆盖范围

### ✅ 端到端功能测试

1. **多语言翻译测试**
   - 中文 → 英文翻译
   - 日文 → 英文翻译
   - 韩文 → 英文翻译
   - 英文输入处理

2. **提交消息格式测试**
   - 多行提交消息
   - Conventional Commits 格式
   - 包含代码引用的消息
   - 包含特殊字符的消息

3. **性能测试**
   - 响应时间验证（< 30秒）
   - 长文本处理

4. **模型兼容性测试**
   - gpt-4o-mini 模型
   - gpt-5-nano 模型（可选）

5. **错误处理测试**
   - 无效 API Key
   - 无效 API URL
   - 边界情况（空文本、超长文本）

## 测试示例

### 测试输出示例

```
TranslationServiceIntegrationTest > test translateToEnglish with Chinese text returns English translation PASSED

Original: 修复：更新API端点配置
Translated: fix: update API endpoint configuration

TranslationServiceIntegrationTest > test translateToEnglish with Japanese text returns English translation PASSED

Original: 機能：新しいユーザー認証システムを追加
Translated: feat: add new user authentication system
```

## 跳过机制

如果未设置 `TEST_OPENAI_API_KEY`，测试会自动跳过并显示消息：

```
Integration tests skipped: TEST_OPENAI_API_KEY environment variable not set.
Set it to run real API integration tests.
```

这确保了：
- ✅ 不会在没有配置的情况下失败
- ✅ CI/CD 可以安全运行而无需配置
- ✅ 本地开发者可以选择性运行

## 成本注意事项

### 💰 API 调用成本

集成测试会发起**真实的 API 调用**，会消耗：
- API 配额
- 计费额度（如果使用付费 API）

### 建议

1. **使用专用测试 API Key**
   - 为测试创建单独的 API Key
   - 设置较低的使用限额

2. **本地运行时选择性执行**
   ```bash
   # 只运行需要的测试
   ./gradlew test --tests 'TranslationServiceIntegrationTest.test translateToEnglish with Chinese text*'
   ```

3. **使用便宜的模型**
   ```bash
   export TEST_OPENAI_MODEL="gpt-4o-mini"  # 更便宜的模型
   ```

4. **监控使用量**
   - 定期检查 API 使用情况
   - 设置使用告警

## GitHub Actions CI/CD

### 配置 CI 运行集成测试

1. **在 GitHub 仓库中添加 Secret**：
   - 访问：Settings > Secrets and variables > Actions
   - 添加：`TEST_OPENAI_API_KEY` = 你的测试 API Key

2. **更新 workflow 文件**（可选）：

如果你想在 CI 中运行集成测试，可以更新 `.github/workflows/build.yml`：

```yaml
test:
  name: Test
  needs: [build]
  runs-on: ubuntu-latest
  steps:
    - name: Run Unit Tests
      run: ./gradlew test --tests '*Test' --exclude-tests '*IntegrationTest'

    - name: Run Integration Tests
      if: ${{ secrets.TEST_OPENAI_API_KEY != '' }}
      env:
        TEST_OPENAI_API_KEY: ${{ secrets.TEST_OPENAI_API_KEY }}
      run: ./gradlew test --tests '*IntegrationTest'
```

### 当前 CI 行为

**默认情况下**：
- CI 会运行所有测试
- 集成测试会自动跳过（因为没有设置 API Key）
- 不会导致构建失败
- 只有单元测试会被执行

这样可以：
- ✅ 保护 API Key 安全
- ✅ 避免不必要的 API 调用
- ✅ 保持快速的 CI 反馈

## 测试最佳实践

### ✅ 推荐做法

1. **本地开发时运行**
   - 在提交前运行集成测试
   - 验证端到端功能正常

2. **使用测试专用账号**
   - 不要使用生产 API Key
   - 设置合理的使用限额

3. **定期运行**
   - 每周或每月运行完整测试套件
   - 确保与最新 API 兼容

4. **监控测试结果**
   - 记录测试输出
   - 关注翻译质量变化

### ❌ 避免做法

1. **不要频繁运行所有测试**
   - 避免不必要的 API 调用
   - 选择性运行需要的测试

2. **不要提交 API Key**
   - 使用环境变量
   - 不要硬编码在代码中

3. **不要在每次 CI 运行时都测试**
   - 只在必要时启用（如发布前）
   - 或使用定时任务而非每次提交

## 故障排查

### 测试失败：API Key 无效

```
Error message: API request failed (401): {"error": {"message": "Invalid API key"}}
```

**解决方法**：
1. 检查 API Key 是否正确
2. 确认 API Key 未过期
3. 验证 API Key 有正确的权限

### 测试失败：网络超时

```
Error: java.net.SocketTimeoutException: timeout
```

**解决方法**：
1. 检查网络连接
2. 验证 API URL 是否正确
3. 尝试增加超时时间（当前：60秒）

### 测试被跳过

```
Integration tests skipped: TEST_OPENAI_API_KEY environment variable not set.
```

**解决方法**：
```bash
export TEST_OPENAI_API_KEY="your-key"
./gradlew test --tests TranslationServiceIntegrationTest
```

### 翻译质量不符合预期

**这是正常的**！AI 模型的输出可能会变化。测试验证：
- ✅ 返回成功结果
- ✅ 返回非空文本
- ✅ 包含相关关键词

**不验证**：
- ❌ 精确的翻译文本
- ❌ 特定的措辞

### 测试运行时间过长

**原因**：
- API 调用需要时间（通常 2-5 秒每次）
- 网络延迟

**解决方法**：
- 这是正常的，集成测试本身就比单元测试慢
- 使用 `--tests` 参数只运行特定测试
- 考虑并行运行（Gradle 自动处理）

## 测试与单元测试的对比

### 单元测试（TranslationServiceTest）

- **位置**：`src/test/kotlin/.../services/TranslationServiceTest.kt`
- **类型**：单元测试
- **API 调用**：否
- **运行速度**：快（毫秒级）
- **成本**：无
- **用途**：验证配置逻辑、模型检测、序列化

### 集成测试（TranslationServiceIntegrationTest）

- **位置**：`src/test/kotlin/.../integration/TranslationServiceIntegrationTest.kt`
- **类型**：端到端集成测试
- **API 调用**：是（真实 HTTP 请求）
- **运行速度**：慢（秒级）
- **成本**：消耗 API 配额
- **用途**：验证真实 API 交互、翻译质量

## 添加新的集成测试

### 示例模板

```kotlin
fun `test translateToEnglish with your scenario`() {
    // Arrange
    val inputText = "你的测试输入"

    // Act
    val result = service.translateToEnglish(inputText)

    // Assert
    assertTrue("Translation should succeed", result.isSuccess)

    val translated = result.getOrNull()
    assertNotNull("Translated text should not be null", translated)

    println("Original: $inputText")
    println("Translated: $translated")

    // 添加你的验证逻辑
    assertTrue("Your validation", someCondition)
}
```

### 测试命名约定

使用描述性的测试名称：
```kotlin
fun `test translateToEnglish with [scenario] [expected behavior]`()
```

例如：
- `test translateToEnglish with Chinese text returns English translation`
- `test translateToEnglish with invalid API key returns detailed error`
- `test translateToEnglish performance is reasonable`

## 其他支持的 API 提供商

集成测试支持任何 OpenAI 兼容的 API：

### DeepSeek

```bash
export TEST_OPENAI_API_KEY="your-deepseek-key"
export TEST_OPENAI_API_URL="https://api.deepseek.com/v1/chat/completions"
export TEST_OPENAI_MODEL="deepseek-chat"
./gradlew test --tests TranslationServiceIntegrationTest
```

### Azure OpenAI

```bash
export TEST_OPENAI_API_KEY="your-azure-key"
export TEST_OPENAI_API_URL="https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2024-02-15"
export TEST_OPENAI_MODEL="gpt-4"
./gradlew test --tests TranslationServiceIntegrationTest
```

### 本地 LLM（Ollama、LM Studio）

```bash
export TEST_OPENAI_API_KEY="dummy-key"
export TEST_OPENAI_API_URL="http://localhost:11434/v1/chat/completions"
export TEST_OPENAI_MODEL="llama2"
./gradlew test --tests TranslationServiceIntegrationTest
```

## 快速开始检查清单

- [ ] 设置 `TEST_OPENAI_API_KEY` 环境变量
- [ ] （可选）设置 `TEST_OPENAI_API_URL` 和 `TEST_OPENAI_MODEL`
- [ ] 运行 `./gradlew test --tests TranslationServiceIntegrationTest`
- [ ] 查看测试输出和翻译结果
- [ ] 确认所有测试通过

## 相关文档

- 📖 [单元测试文档](src/test/README.md)
- 📖 [测试 API Key 配置指南](TEST_API_KEY_CONFIG_CN.md)
- 📖 [自动化测试说明](TESTING_CN.md)
- 📖 [主 README](README.md)

## 总结

✅ **集成测试已完全配置**
- 真实 API 调用验证端到端功能
- 环境变量配置，安全可靠
- 自动跳过机制，不影响正常开发
- 支持多语言、多场景测试
- 包含性能和错误处理验证

现在你可以完全信任 `translateToEnglish` 方法在生产环境中的行为！
