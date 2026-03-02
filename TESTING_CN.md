# GitHub Actions 自动化测试说明

## 概述

本项目已配置 GitHub Actions CI/CD 来自动测试 `translateToEnglish` 接口。每次代码推送或提交 Pull Request 时，测试会自动运行。

## 自动化测试配置

### 1. 测试文件位置

```
src/test/kotlin/com/github/darleywey/committranslator/services/
└── TranslationServiceTest.kt  # TranslationService 的单元测试
```

### 2. GitHub Actions 工作流

**工作流文件**: `.github/workflows/build.yml`

**触发条件**:
- 推送到 `main` 分支
- 所有 Pull Request

**测试执行步骤**:
```yaml
# 第 111-112 行
- name: Run Tests
  run: ./gradlew check
```

### 3. 测试执行流程

```
1. 代码推送/PR 创建
   ↓
2. GitHub Actions 触发
   ↓
3. 设置环境 (Ubuntu + Java 21)
   ↓
4. 运行 ./gradlew check
   ↓
5. 执行所有单元测试
   ↓
6. 生成代码覆盖率报告
   ↓
7. 上传到 CodeCov
   ↓
8. 显示测试结果
```

## 测试覆盖范围

### TranslationService 接口测试

#### ✅ 配置验证测试
- API Key 为空时的错误处理
- API URL 为空时的错误处理
- 配置缺失的各种场景

#### ✅ 模型检测测试
- GPT-5 系列模型（gpt-5-nano 等）
- O1 系列模型（o1-preview 等）
- O3 系列模型
- 旧版模型（gpt-4o-mini 等）
- 大小写不敏感匹配

#### ✅ 请求序列化测试
- `max_tokens` 参数（旧版模型）
- `max_completion_tokens` 参数（新版模型）
- JSON 序列化正确性

#### ✅ 响应解析测试
- 有效 API 响应反序列化
- 多个选择的处理
- 未知字段的忽略（前向兼容）

#### ✅ 错误处理测试
- 网络错误
- API 错误响应
- 完整错误信息显示

#### ✅ 服务配置测试
- 单例模式验证
- API Key 持久化
- URL 配置
- 模型配置

### 测试统计

- **测试类**: 1 个
- **测试方法**: 30+ 个
- **测试覆盖**: 配置、模型检测、序列化、响应解析、错误处理

## 查看测试结果

### 在 GitHub 上查看

1. 访问仓库的 **Actions** 标签页
2. 选择最近的工作流运行
3. 点击 **Test** 作业
4. 查看 "Run Tests" 步骤的输出

### 测试失败时

如果测试失败：
1. GitHub Actions 会标记构建失败 ❌
2. 测试报告会上传为 Artifact
3. 可以下载 `tests-result` 查看详细报告
4. Pull Request 会显示测试失败状态

### 本地运行测试

```bash
# 运行所有测试
./gradlew test

# 运行测试并生成覆盖率报告
./gradlew check

# 查看覆盖率报告
./gradlew koverHtmlReport
open build/reports/kover/html/index.html
```

## 代码覆盖率

### Kover 集成

项目使用 **Gradle Kover** 插件跟踪代码覆盖率。

**配置位置**: `build.gradle.kts`
```kotlin
kover {
    reports {
        total {
            xml {
                onCheck = true  // 运行 check 时生成 XML 报告
            }
        }
    }
}
```

### CodeCov 集成

覆盖率报告自动上传到 **CodeCov**。

**工作流配置**:
```yaml
- name: Upload Code Coverage Report
  uses: codecov/codecov-action@v5
  with:
    files: ${{ github.workspace }}/build/reports/kover/report.xml
    token: ${{ secrets.CODECOV_TOKEN }}
```

**查看覆盖率**:
1. 访问 [CodeCov Dashboard](https://codecov.io/)
2. 找到 `Darley-Wey/Commit-Translator` 项目
3. 查看覆盖率趋势和详细报告

## CI/CD 工作流详情

### 完整 CI 流程

```
jobs:
  ├── build          # 构建插件
  ├── test           # 运行测试 + 覆盖率
  ├── inspectCode    # 代码质量检查 (Qodana)
  ├── verify         # 插件验证
  └── releaseDraft   # 创建发布草稿（仅 main 分支）
```

### Test 作业详情

**运行环境**:
- OS: Ubuntu Latest
- Java: 21 (Zulu)
- Gradle: 使用 wrapper

**执行步骤**:
1. ✅ 释放磁盘空间
2. ✅ 检出代码
3. ✅ 设置 Java 21
4. ✅ 设置 Gradle（启用缓存）
5. ✅ 运行测试 (`./gradlew check`)
6. ✅ 收集测试结果（失败时）
7. ✅ 上传覆盖率到 CodeCov

## 测试最佳实践

### 1. 测试隔离
每个测试独立运行，不依赖外部服务：
```kotlin
fun `test translateToEnglish fails when API key is not configured`() {
    // Arrange: 设置测试条件
    settings.apiKey = ""

    // Act: 执行被测代码
    val result = service.translateToEnglish("测试消息")

    // Assert: 验证结果
    assertTrue(result.isFailure)
}
```

### 2. 清晰的测试命名
使用反引号语法让测试名称更易读：
```kotlin
fun `test requiresMaxCompletionTokens returns true for gpt-5 models`() { ... }
```

### 3. AAA 模式
遵循 Arrange-Act-Assert 模式：
- **Arrange**: 设置测试数据
- **Act**: 执行操作
- **Assert**: 验证结果

### 4. 测试各种场景
- ✅ 成功场景
- ✅ 失败场景
- ✅ 边界条件
- ✅ 错误处理

## 添加新测试

### 步骤

1. 在 `src/test/kotlin/.../services/` 添加测试方法
2. 使用描述性的测试名称
3. 遵循 AAA 模式
4. 提交代码
5. GitHub Actions 自动运行新测试

### 示例

```kotlin
fun `test new feature works correctly`() {
    // Arrange
    settings.apiKey = "test-key"
    settings.model = "gpt-5-nano"

    // Act
    val result = service.translateToEnglish("新功能测试")

    // Assert
    assertTrue(result.isSuccess)
}
```

## 故障排查

### 测试失败

**常见原因**:
1. 配置错误（API Key、URL）
2. 模型名称检测逻辑问题
3. 序列化/反序列化错误
4. 响应解析问题

**调试步骤**:
1. 查看 GitHub Actions 日志
2. 下载测试报告 Artifact
3. 本地运行失败的测试
4. 检查错误消息

### 本地测试通过但 CI 失败

可能原因：
- 环境差异（Java 版本、OS）
- 时区问题
- 文件路径问题
- 并发问题

### CI 测试通过但本地失败

可能原因：
- 本地缓存问题
- 依赖版本不一致
- 本地配置文件干扰

**解决方法**:
```bash
./gradlew clean
./gradlew --refresh-dependencies
./gradlew test --rerun-tasks
```

## 持续改进

### 未来计划

1. **集成测试**: 添加带 Mock HTTP 服务器的集成测试
2. **性能测试**: 添加翻译速度基准测试
3. **UI 测试**: 添加 TranslateCommitMessageAction 的 UI 测试
4. **契约测试**: 添加 API 契约验证

### 监控指标

- ✅ 测试通过率: 100%
- ✅ 代码覆盖率: 由 CodeCov 跟踪
- ✅ 构建时间: GitHub Actions 报告
- ✅ 测试执行时间: 测试报告显示

## 相关资源

- **测试文档**: `src/test/README.md`
- **工作流文件**: `.github/workflows/build.yml`
- **覆盖率配置**: `codecov.yml`
- **构建配置**: `build.gradle.kts`

## 总结

✅ **自动化测试已完全配置**
- 30+ 单元测试覆盖 `translateToEnglish` 接口
- GitHub Actions 自动运行所有测试
- 代码覆盖率自动上传到 CodeCov
- 测试失败时自动上传报告
- 支持本地和 CI 环境运行

每次代码变更都会自动触发完整的测试套件，确保代码质量！
