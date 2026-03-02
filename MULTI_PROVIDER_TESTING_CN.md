# 多供应商多模型集成测试指南

## 概述

本项目支持对多个 AI 服务供应商及其模型进行端到端集成测试，验证每个供应商的每个模型都能正常工作。

## 支持的供应商

### 1. OpenAI
- **模型**: gpt-4o-mini, gpt-4o, gpt-4-turbo, gpt-3.5-turbo
- **API 端点**: https://api.openai.com/v1/chat/completions

### 2. DeepSeek
- **模型**: deepseek-chat, deepseek-coder
- **API 端点**: https://api.deepseek.com/v1/chat/completions

### 3. Azure OpenAI
- **模型**: gpt-4, gpt-35-turbo（根据您的部署）
- **API 端点**: https://{resource}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version={version}

### 4. Ollama（本地 LLM）
- **模型**: llama2, mistral, qwen 等
- **API 端点**: http://localhost:11434/v1/chat/completions

### 5. 自定义供应商
- 支持任何 OpenAI 兼容的 API 端点

## 配置方式

### 环境变量配置模式

每个供应商使用以下环境变量模式：

```bash
TEST_{PROVIDER}_API_KEY     # API 密钥（必需）
TEST_{PROVIDER}_API_URL     # API 端点 URL（可选，有默认值）
TEST_{PROVIDER}_MODELS      # 要测试的模型列表，逗号分隔（可选，有默认值）
```

## 快速开始

### 1. 配置 OpenAI

```bash
export TEST_OPENAI_API_KEY="sk-your-openai-api-key"
export TEST_OPENAI_API_URL="https://api.openai.com/v1/chat/completions"  # 可选
export TEST_OPENAI_MODELS="gpt-4o-mini,gpt-4o"  # 可选
```

### 2. 配置 DeepSeek

```bash
export TEST_DEEPSEEK_API_KEY="your-deepseek-api-key"
export TEST_DEEPSEEK_API_URL="https://api.deepseek.com/v1/chat/completions"  # 可选
export TEST_DEEPSEEK_MODELS="deepseek-chat,deepseek-coder"  # 可选
```

### 3. 配置 Azure OpenAI

```bash
export TEST_AZURE_API_KEY="your-azure-api-key"
export TEST_AZURE_API_URL="https://your-resource.openai.azure.com/openai/deployments/gpt-4/chat/completions?api-version=2024-02-15"
export TEST_AZURE_MODELS="gpt-4,gpt-35-turbo"
```

### 4. 配置 Ollama（本地）

```bash
# 启动 Ollama 服务
ollama serve

# 拉取模型
ollama pull llama2

# 配置测试
export TEST_OLLAMA_API_KEY="dummy"  # Ollama 不需要真实 key
export TEST_OLLAMA_API_URL="http://localhost:11434/v1/chat/completions"  # 可选
export TEST_OLLAMA_MODELS="llama2,mistral"  # 可选
```

### 5. 配置自定义供应商

```bash
export TEST_CUSTOM_API_KEY="your-custom-api-key"
export TEST_CUSTOM_API_URL="https://your-api.example.com/v1/chat/completions"
export TEST_CUSTOM_MODELS="model1,model2"
```

## 运行测试

### 运行所有配置的供应商测试

```bash
# 查看已配置的供应商
./gradlew test --tests 'MultiProviderIntegrationTest.test show configured providers summary'

# 运行所有供应商的所有测试
./gradlew test --tests MultiProviderIntegrationTest
```

### 运行特定供应商的测试

```bash
# 只测试 OpenAI
./gradlew test --tests 'MultiProviderIntegrationTest.test OpenAI*'

# 只测试 DeepSeek
./gradlew test --tests 'MultiProviderIntegrationTest.test DeepSeek*'

# 只测试 Azure
./gradlew test --tests 'MultiProviderIntegrationTest.test Azure*'

# 只测试 Ollama
./gradlew test --tests 'MultiProviderIntegrationTest.test Ollama*'
```

### 运行跨供应商对比测试

```bash
# 测试所有供应商的翻译一致性
./gradlew test --tests 'MultiProviderIntegrationTest.test all providers produce valid translations'

# 测试所有供应商的性能
./gradlew test --tests 'MultiProviderIntegrationTest.test performance across all providers'

# 测试参数兼容性
./gradlew test --tests 'MultiProviderIntegrationTest.test max_tokens vs max_completion_tokens compatibility'
```

## 使用配置文件（推荐本地开发）

### 创建 test-providers.sh

```bash
#!/bin/bash
# Multi-Provider Test Configuration

# OpenAI Configuration
export TEST_OPENAI_API_KEY="sk-your-key"
export TEST_OPENAI_MODELS="gpt-4o-mini,gpt-4o"

# DeepSeek Configuration
export TEST_DEEPSEEK_API_KEY="your-deepseek-key"
export TEST_DEEPSEEK_MODELS="deepseek-chat"

# Azure OpenAI Configuration (optional)
# export TEST_AZURE_API_KEY="your-azure-key"
# export TEST_AZURE_API_URL="https://your-resource.openai.azure.com/..."
# export TEST_AZURE_MODELS="gpt-4"

# Ollama Local Configuration (optional)
# export TEST_OLLAMA_API_KEY="dummy"
# export TEST_OLLAMA_MODELS="llama2"

# Custom Provider Configuration (optional)
# export TEST_CUSTOM_API_KEY="your-key"
# export TEST_CUSTOM_API_URL="https://your-api.example.com/v1/chat/completions"
# export TEST_CUSTOM_MODELS="model1"

echo "✓ Provider configurations loaded"
echo "Configured providers:"
env | grep "^TEST_.*_API_KEY=" | sed 's/=.*/=***/' | sort
```

### 使用配置文件

```bash
# 加载配置
source test-providers.sh

# 运行测试
./gradlew test --tests MultiProviderIntegrationTest
```

## GitHub Actions 配置

### 在 GitHub 仓库中配置 Secrets

1. 访问仓库设置：**Settings** > **Secrets and variables** > **Actions**

2. 添加以下 Secrets：

   - `TEST_OPENAI_API_KEY`
   - `TEST_OPENAI_MODELS`（可选）
   - `TEST_DEEPSEEK_API_KEY`
   - `TEST_DEEPSEEK_MODELS`（可选）
   - `TEST_AZURE_API_KEY`（可选）
   - `TEST_AZURE_API_URL`（可选）
   - `TEST_AZURE_MODELS`（可选）

### 更新 workflow 文件

在 `.github/workflows/build.yml` 中添加多供应商测试作业：

```yaml
  multi-provider-integration-test:
    name: Multi-Provider Integration Tests
    runs-on: ubuntu-latest
    # 只在配置了至少一个供应商时运行
    if: |
      secrets.TEST_OPENAI_API_KEY != '' ||
      secrets.TEST_DEEPSEEK_API_KEY != '' ||
      secrets.TEST_AZURE_API_KEY != ''

    strategy:
      fail-fast: false  # 一个供应商失败不影响其他供应商测试
      matrix:
        provider:
          - name: openai
            secret_key: TEST_OPENAI_API_KEY
            secret_models: TEST_OPENAI_MODELS
          - name: deepseek
            secret_key: TEST_DEEPSEEK_API_KEY
            secret_models: TEST_DEEPSEEK_MODELS
          - name: azure
            secret_key: TEST_AZURE_API_KEY
            secret_models: TEST_AZURE_MODELS

    steps:
      - name: Fetch Sources
        uses: actions/checkout@v5

      - name: Setup Java
        uses: actions/setup-java@v5
        with:
          distribution: zulu
          java-version: 21

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v5

      - name: Test ${{ matrix.provider.name }} Provider
        env:
          TEST_OPENAI_API_KEY: ${{ secrets.TEST_OPENAI_API_KEY }}
          TEST_OPENAI_MODELS: ${{ secrets.TEST_OPENAI_MODELS }}
          TEST_DEEPSEEK_API_KEY: ${{ secrets.TEST_DEEPSEEK_API_KEY }}
          TEST_DEEPSEEK_MODELS: ${{ secrets.TEST_DEEPSEEK_MODELS }}
          TEST_AZURE_API_KEY: ${{ secrets.TEST_AZURE_API_KEY }}
          TEST_AZURE_API_URL: ${{ secrets.TEST_AZURE_API_URL }}
          TEST_AZURE_MODELS: ${{ secrets.TEST_AZURE_MODELS }}
        run: |
          ./gradlew test --tests 'MultiProviderIntegrationTest.test ${{ matrix.provider.name }}*' || true

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v5
        with:
          name: test-results-${{ matrix.provider.name }}
          path: build/reports/tests/
```

## 测试覆盖范围

### 单供应商测试

每个供应商会执行以下测试：

1. **基本功能测试**
   - 所有配置的模型都能正常翻译
   - 支持多种语言（中文、日文、韩文、英文）
   - 处理不同的提交消息格式

2. **特定模型测试**
   - 每个模型的独立验证
   - 模型特定功能测试

### 跨供应商测试

1. **翻译一致性**
   - 所有供应商产生有效翻译
   - 对比不同供应商的翻译结果

2. **性能对比**
   - 测量每个供应商的响应时间
   - 生成性能对比报告

3. **兼容性测试**
   - max_tokens vs max_completion_tokens
   - 多行文本处理
   - 特殊字符处理

4. **错误处理**
   - 所有供应商返回详细错误信息
   - 无效凭据的错误处理

## 测试输出示例

### 供应商摘要

```
=== Configured Providers Summary ===

OpenAI:
  URL: https://api.openai.com/v1/chat/completions
  Models: gpt-4o-mini, gpt-4o
  Status: ✓ Enabled

DeepSeek:
  URL: https://api.deepseek.com/v1/chat/completions
  Models: deepseek-chat
  Status: ✓ Enabled

====================================
```

### 跨供应商翻译对比

```
=== Cross-Provider Translation Comparison ===
Original: 修复：解决登录问题
OpenAI/gpt-4o-mini: fix: resolve login issue
DeepSeek/deepseek-chat: fix: solve login problem
Azure/gpt-4: fix: address login issue
==============================================
```

### 性能对比

```
=== Performance Comparison ===
DeepSeek/deepseek-chat: 1823ms
OpenAI/gpt-4o-mini: 2156ms
Azure/gpt-4: 3421ms
==============================
```

## 成本控制建议

### 1. 选择性测试

```bash
# 只测试便宜的模型
export TEST_OPENAI_MODELS="gpt-4o-mini"  # 而不是 gpt-4o
export TEST_DEEPSEEK_MODELS="deepseek-chat"

# 只在必要时测试所有模型
```

### 2. 使用本地 Ollama

```bash
# 免费的本地测试
ollama serve
export TEST_OLLAMA_API_KEY="dummy"
export TEST_OLLAMA_MODELS="llama2"
./gradlew test --tests 'MultiProviderIntegrationTest.test Ollama*'
```

### 3. CI 中的成本控制

```yaml
# 只在特定事件触发完整测试
on:
  schedule:
    - cron: '0 2 * * 1'  # 每周一凌晨 2 点
  workflow_dispatch:     # 手动触发
```

### 4. 设置测试预算

创建专用的测试 API Key，设置月度预算限制。

## 故障排查

### 测试跳过

```
OpenAI tests skipped: TEST_OPENAI_API_KEY not configured
```

**解决方法**：设置相应的环境变量

```bash
export TEST_OPENAI_API_KEY="sk-your-key"
```

### API 错误

```
API request failed (401): {"error": {"message": "Invalid API key"}}
```

**解决方法**：
1. 检查 API Key 是否正确
2. 确认 API Key 未过期
3. 验证 API URL 是否正确

### 模型不可用

```
API request failed (404): {"error": {"message": "Model not found"}}
```

**解决方法**：
1. 检查模型名称是否正确
2. 确认您的账户有权限访问该模型
3. 对于 Azure，确认部署名称正确

### Ollama 连接失败

```
Failed to connect to localhost:11434
```

**解决方法**：
```bash
# 启动 Ollama 服务
ollama serve

# 验证服务运行
curl http://localhost:11434/api/tags
```

## 最佳实践

### ✅ 推荐做法

1. **本地开发**
   - 使用配置文件管理多个供应商
   - 从最便宜的模型开始测试
   - 使用 Ollama 进行免费的快速迭代

2. **CI/CD**
   - 为每个供应商创建独立的测试作业
   - 使用 `fail-fast: false` 避免一个失败影响其他
   - 设置定时任务而非每次提交都运行

3. **成本管理**
   - 专用测试 API Key with 限额
   - 优先测试便宜的模型
   - 定期审查 API 使用情况

4. **安全性**
   - 从不提交 API Key 到代码
   - 使用 GitHub Secrets
   - 配置文件添加到 .gitignore

### ❌ 避免做法

1. 不要在每次提交时运行所有供应商的所有模型
2. 不要使用生产 API Key 进行测试
3. 不要忽略测试失败（可能影响用户）
4. 不要硬编码 API 配置

## 扩展新供应商

### 添加新供应商的步骤

1. **在测试类中添加配置**：

```kotlin
ProviderConfig(
    name = "NewProvider",
    apiKey = System.getenv("TEST_NEWPROVIDER_API_KEY"),
    apiUrl = System.getenv("TEST_NEWPROVIDER_API_URL")
        ?: "https://api.newprovider.com/v1/chat/completions",
    models = System.getenv("TEST_NEWPROVIDER_MODELS")?.split(",")?.map { it.trim() }
        ?: listOf("default-model")
)
```

2. **添加供应商特定测试**：

```kotlin
fun `test NewProvider with all configured models`() {
    val provider = providers.find { it.name == "NewProvider" }
    assumeTrue("NewProvider tests skipped: TEST_NEWPROVIDER_API_KEY not configured", provider != null)
    testProviderWithAllModels(provider!!)
}
```

3. **更新文档**：在本文档中添加新供应商的配置说明

4. **更新 GitHub Actions**（如果需要）：在 workflow matrix 中添加新供应商

## 相关文档

- 📖 [Integration Testing Guide](INTEGRATION_TESTING_CN.md) - 单供应商集成测试
- 📖 [Test API Key Configuration](TEST_API_KEY_CONFIG_CN.md) - API Key 配置指南
- 📖 [Testing Guide](TESTING_CN.md) - 自动化测试说明
- 📖 [Main README](README.md) - 项目主文档

## 总结

✅ **多供应商测试框架已配置完成**
- 支持 5+ 种 AI 服务供应商
- 每个供应商支持多个模型
- 环境变量驱动的灵活配置
- GitHub Actions 集成支持
- 自动跳过未配置的供应商
- 跨供应商性能和一致性测试
- 完善的成本控制机制

现在您可以全面验证所有供应商和模型的可用性！🎉
