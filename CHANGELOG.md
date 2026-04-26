<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Commit Translator Changelog

## [Unreleased]

## [0.0.4] - 2026-04-26

### Changed

- Expanded IDE compatibility to IntelliJ Platform 2024.2+

## [0.0.3] - 2026-03-03

### Added

- Connection test panel with custom input/output for testing translations in settings
- Full Chinese (Simplified) localization for all error messages and validation prompts

### Fixed

- Fixed settings panel layout and field alignment
- Fixed some models rejecting requests due to unsupported default parameters
- API error messages now properly returned to the user

## [0.0.2] - 2026-01-24

### Changed

- Improved commit message detection for various IntelliJ VCS panels
- Updated plugin display name (removed hyphen)

### Fixed

- Fixed Locale compatibility errors in non-English environments
- Fixed emoji encoding issues in plugin description
- Fixed Marketplace badge URLs

## [0.0.1] - 2025-01-20

### Added

- Initial release
- Translate commit messages to English using OpenAI-compatible APIs
- Support for OpenAI, Azure OpenAI, DeepSeek, and other compatible APIs
- Secure API key storage using IDE's credential store
- Configurable API endpoint, model settings
- English and Chinese (Simplified) localization

[Unreleased]: https://github.com/Darley-Wey/Commit-Translator/compare/v0.0.4...HEAD
[0.0.4]: https://github.com/Darley-Wey/Commit-Translator/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/Darley-Wey/Commit-Translator/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/Darley-Wey/Commit-Translator/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/Darley-Wey/Commit-Translator/commits/v0.0.1
