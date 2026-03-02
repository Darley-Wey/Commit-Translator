# Test Documentation

## Overview

This directory contains automated tests for the Commit-Translator plugin. Tests are written using JUnit 4 and the IntelliJ Platform Test Framework.

## Test Structure

```
src/test/kotlin/
└── com/github/darleywey/committranslator/
    └── services/
        └── TranslationServiceTest.kt
```

## Running Tests

### Local Development

Run all tests:
```bash
./gradlew test
```

Run tests with coverage report:
```bash
./gradlew check
./gradlew koverXmlReport
```

View coverage report:
```bash
open build/reports/kover/html/index.html
```

### GitHub Actions CI

Tests are automatically run on:
- Every push to the `main` branch
- Every pull request

The workflow file is located at `.github/workflows/build.yml`.

Test execution in CI:
1. **Build Job**: Compiles the plugin
2. **Test Job**: Runs `./gradlew check` which includes:
   - All unit tests
   - Code coverage with Kover
   - Uploads results to CodeCov

## TranslationServiceTest

### Test Coverage

The `TranslationServiceTest` class provides comprehensive coverage for the `translateToEnglish` interface:

#### 1. Configuration Validation Tests
- ✅ Empty API key handling
- ✅ Empty API URL handling
- ✅ Both API key and URL blank

#### 2. Model Detection Tests
- ✅ GPT-5 series models (gpt-5-nano, gpt-5-mini, etc.)
- ✅ O1 series models (o1, o1-preview, o1-mini)
- ✅ O3 series models (o3, o3-mini)
- ✅ Older models (gpt-4o-mini, gpt-4-turbo, gpt-3.5-turbo)
- ✅ Case-insensitive model name matching

#### 3. Request Serialization Tests
- ✅ ChatRequest with `max_tokens` parameter (older models)
- ✅ ChatRequest with `max_completion_tokens` parameter (newer models)
- ✅ Proper JSON serialization of null fields

#### 4. Response Parsing Tests
- ✅ Valid API response deserialization
- ✅ Multiple choices handling
- ✅ Unknown fields ignored (forward compatibility)

#### 5. Service Configuration Tests
- ✅ Singleton pattern verification
- ✅ API key persistence
- ✅ URL configuration
- ✅ Model configuration
- ✅ Default values

#### 6. Input Validation Tests
- ✅ Non-empty text acceptance
- ✅ Various commit message formats
- ✅ Multi-line messages
- ✅ Chinese and English text

## Test Methodology

### Unit Testing Approach

The tests follow these principles:

1. **Isolation**: Each test is independent and doesn't rely on external services
2. **Fast**: Tests run quickly without network calls (unless intentional integration tests)
3. **Deterministic**: Tests produce consistent results
4. **Clear**: Test names describe what is being tested using backtick notation

### Test Naming Convention

Tests use Kotlin's backtick syntax for descriptive names:
```kotlin
fun `test translateToEnglish fails when API key is not configured`() { ... }
```

This makes tests self-documenting and easy to understand.

### AAA Pattern

Tests follow the Arrange-Act-Assert pattern:
```kotlin
fun `test example`() {
    // Arrange: Set up test data and conditions
    settings.apiKey = "test-key"

    // Act: Execute the code under test
    val result = service.translateToEnglish("test")

    // Assert: Verify the expected outcome
    assertTrue(result.isSuccess)
}
```

## Code Coverage

Code coverage is tracked using **Gradle Kover** and reported to **CodeCov**.

Coverage reports are generated at:
- XML: `build/reports/kover/report.xml` (for CI)
- HTML: `build/reports/kover/html/index.html` (for local viewing)

Current coverage target: **informational** (no strict threshold)

## Adding New Tests

When adding new tests:

1. Place test files in the same package structure as the source files
2. Use descriptive test names with backticks
3. Follow the AAA pattern
4. Add tests for both success and failure cases
5. Test edge cases and boundary conditions
6. Update this README with new test categories

### Example Test Template

```kotlin
fun `test feature does expected behavior`() {
    // Arrange
    val input = "test input"
    settings.apiKey = "test-key"

    // Act
    val result = service.translateToEnglish(input)

    // Assert
    assertTrue(result.isSuccess)
    assertEquals("expected output", result.getOrNull())
}
```

## Continuous Integration

### GitHub Actions Workflow

The test workflow (`.github/workflows/build.yml`) includes:

1. **Environment Setup**
   - Ubuntu latest
   - Java 21 (Zulu distribution)
   - Gradle with caching

2. **Test Execution**
   - Runs: `./gradlew check`
   - Timeout: Default (no explicit timeout)
   - Parallelization: Gradle default

3. **Artifact Collection**
   - Test results uploaded on failure
   - Location: `build/reports/tests`

4. **Coverage Upload**
   - Automatic upload to CodeCov
   - Uses repository secret: `CODECOV_TOKEN`

### Test Failure Handling

When tests fail in CI:
1. Test results are uploaded as artifacts
2. Build fails and prevents merge
3. Developers can download test reports from GitHub Actions artifacts

## Troubleshooting

### Tests Fail Locally But Pass in CI

- Check Java version: Tests require Java 21
- Clean build: `./gradlew clean`
- Update dependencies: `./gradlew --refresh-dependencies`

### Tests Fail in CI But Pass Locally

- Check for timing issues or race conditions
- Verify no dependencies on local environment
- Check for hardcoded paths or configurations

### Coverage Reports Not Generated

- Ensure Kover plugin is configured in `build.gradle.kts`
- Run: `./gradlew clean check koverXmlReport`
- Check: `build/reports/kover/` directory exists

## Future Improvements

Potential enhancements for the test suite:

1. **Integration Tests**: Add tests with mock HTTP server
2. **Performance Tests**: Add benchmarks for translation speed
3. **UI Tests**: Add tests for the TranslateCommitMessageAction
4. **Property-based Tests**: Add randomized testing
5. **Contract Tests**: Add tests for API contract validation

## Resources

- [IntelliJ Platform Test Framework](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Gradle Kover Plugin](https://kotlin.github.io/kotlinx-kover/)
- [CodeCov Documentation](https://docs.codecov.com/)
