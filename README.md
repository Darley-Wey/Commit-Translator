# Commit Translator

[English](README.md) | [简体中文](README.zh-CN.md)

![Build](https://github.com/Darley-Wey/Commit-Translator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/29868.svg)](https://plugins.jetbrains.com/plugin/29868)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/29868.svg)](https://plugins.jetbrains.com/plugin/29868)

<!-- Plugin description -->
**Commit Translator** is an IntelliJ IDEA plugin that translates your commit messages to English using OpenAI-compatible APIs.

## Features

- **One-Click Translation**: Translate commit messages to English with a single click
- **OpenAI-Compatible**: Works with OpenAI, Azure OpenAI, DeepSeek, and other compatible APIs
- **Secure Storage**: API keys are stored securely using IDE's credential store
- **Customizable**: Configure API endpoint, model, and other settings

## Usage

1. Open the Commit tool window
2. Write your commit message in any language
3. Click the "Translate to English" button in the commit message toolbar
4. Your message will be translated to English automatically

## Configuration

Go to **Settings/Preferences** > **Tools** > **Commit Translator** to configure:

- **API URL**: Your OpenAI-compatible API endpoint
- **API Key**: Your API key (stored securely)
- **Model**: The model to use (e.g., gpt-4o-mini, gpt-4, deepseek-chat)

## Supported APIs

This plugin works with any OpenAI-compatible chat completion API:

- **OpenAI**: `https://api.openai.com/v1/chat/completions`
- **Azure OpenAI**: `https://{resource}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version={version}`
- **DeepSeek**: `https://api.deepseek.com/v1/chat/completions`
- **Local LLMs**: Any local server with OpenAI-compatible API (e.g., Ollama, LM Studio)
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Commit Translator"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/29868) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

- Manually:

  Download the [latest release](https://github.com/Darley-Wey/Commit-Translator/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Gear Icon</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
