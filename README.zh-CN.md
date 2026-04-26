# Commit Translator

[English](README.md) | [简体中文](README.zh-CN.md)

![Build](https://github.com/Darley-Wey/Commit-Translator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/29868.svg)](https://plugins.jetbrains.com/plugin/29868)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/29868.svg)](https://plugins.jetbrains.com/plugin/29868)

**Commit Translator** 是一个 IntelliJ IDEA 插件，可通过兼容 OpenAI 的 API 将你的 commit message 翻译为英文。

## 功能特性

- **一键翻译**：单击即可将 commit message 翻译为英文
- **兼容 OpenAI API**：支持 OpenAI、Azure OpenAI、DeepSeek 以及其他兼容 API
- **安全存储**：API Key 使用 IDE 凭据存储安全保存
- **可自定义**：可配置 API 地址、模型和其他设置

## 使用方式

1. 打开 Commit 工具窗口
2. 用任意语言编写 commit message
3. 点击 commit message 工具栏中的 "Translate to English" 按钮
4. 消息会自动翻译为英文

## 配置说明

前往 **Settings/Preferences** > **Tools** > **Commit Translator**，可配置以下内容：

- **API URL**：你的 OpenAI 兼容 API 地址
- **API Key**：你的 API Key（会被安全存储）
- **Model**：使用的模型（例如 `gpt-4o-mini`、`gpt-4`、`deepseek-chat`）

## 支持的 API

该插件适用于任何兼容 OpenAI Chat Completions 的 API：

- **OpenAI**：`https://api.openai.com/v1/chat/completions`
- **Azure OpenAI**：`https://{resource}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version={version}`
- **DeepSeek**：`https://api.deepseek.com/v1/chat/completions`
- **本地 LLM**：任何提供 OpenAI 兼容 API 的本地服务（如 Ollama、LM Studio）

## 安装方式

- 使用 IDE 内置插件系统：

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>搜索 "Commit Translator"</kbd> >
  <kbd>Install</kbd>

- 使用 JetBrains Marketplace：

  打开 [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29868)，如果 IDE 正在运行，可点击页面中的 <kbd>Install to ...</kbd> 按钮直接安装。

- 手动安装：

  下载[最新发布版本](https://github.com/Darley-Wey/Commit-Translator/releases/latest)，然后通过
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Gear Icon</kbd> > <kbd>Install plugin from disk...</kbd>
  手动安装。

---
基于 [IntelliJ Platform Plugin Template][template] 构建。

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
