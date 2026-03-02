package com.github.darleywey.committranslator.settings

import com.github.darleywey.committranslator.CommitTranslatorBundle
import com.github.darleywey.committranslator.services.TranslationService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JButton
import javax.swing.JComponent

class CommitTranslatorConfigurable : Configurable {

    private var panel: DialogPanel? = null
    private val apiUrlField = JBTextField()
    private val apiKeyField = JBPasswordField()
    private val modelField = JBTextField()
    private val testInputField = JBTextArea(5, 40)
    private val testOutputField = JBTextArea(5, 40)
    private var testButton: JButton? = null

    override fun getDisplayName(): String = CommitTranslatorBundle.message("settings.displayName")

    override fun createComponent(): JComponent {
        val settings = CommitTranslatorSettings.getInstance()

        panel = panel {
            row(CommitTranslatorBundle.message("settings.apiUrl")) {
                cell(apiUrlField)
                    .applyToComponent { columns = 35 }
                    .comment(CommitTranslatorBundle.message("settings.apiUrl.comment"))
            }

            row(CommitTranslatorBundle.message("settings.apiKey")) {
                cell(apiKeyField)
                    .applyToComponent { columns = 35 }
                    .comment(CommitTranslatorBundle.message("settings.apiKey.comment"))
            }

            row(CommitTranslatorBundle.message("settings.model")) {
                cell(modelField)
                    .applyToComponent { columns = 35 }
                    .comment(CommitTranslatorBundle.message("settings.model.comment"))
            }

            separator()

            panel {
                row {
                    panel {
                        row {
                            label(CommitTranslatorBundle.message("settings.testConnection.input"))
                        }
                        row {
                            testInputField.text = CommitTranslatorBundle.message("settings.testConnection.input.default")
                            testInputField.lineWrap = true
                            testInputField.wrapStyleWord = true
                            scrollCell(testInputField)
                                .align(AlignX.LEFT)
                                .applyToComponent {
                                    preferredSize = java.awt.Dimension(350, 150)
                                    minimumSize = java.awt.Dimension(350, 150)
                                }
                        }
                    }.align(AlignX.LEFT)

                    panel {
                        row {
                            label(CommitTranslatorBundle.message("settings.testConnection.output"))
                        }
                        row {
                            testOutputField.isEditable = false
                            testOutputField.lineWrap = true
                            testOutputField.wrapStyleWord = true
                            scrollCell(testOutputField)
                                .align(AlignX.LEFT)
                                .applyToComponent {
                                    preferredSize = java.awt.Dimension(350, 150)
                                    minimumSize = java.awt.Dimension(350, 150)
                                }
                        }
                    }.align(AlignX.LEFT)
                }

                row {
                    testButton = button(CommitTranslatorBundle.message("settings.testConnection")) {
                        testConnection()
                    }.component
                }
            }
        }.apply {
            // Set a preferred width for the entire panel
            preferredSize = java.awt.Dimension(800, 600)
        }

        apiUrlField.text = settings.apiUrl
        apiKeyField.text = settings.apiKey
        modelField.text = settings.model

        return panel!!
    }

    private fun testConnection() {
        val apiUrl = apiUrlField.text.trim()
        val apiKey = String(apiKeyField.password).trim()
        val model = modelField.text.trim()
        val input = testInputField.text.trim()

        if (apiUrl.isEmpty() || apiKey.isEmpty() || model.isEmpty()) {
            Messages.showWarningDialog(
                CommitTranslatorBundle.message("settings.testConnection.fillAllFields"),
                CommitTranslatorBundle.message("settings.testConnection")
            )
            return
        }

        if (input.isEmpty()) {
            Messages.showWarningDialog(
                CommitTranslatorBundle.message("settings.testConnection.provideInput"),
                CommitTranslatorBundle.message("settings.testConnection")
            )
            return
        }

        testButton?.isEnabled = false
        testOutputField.text = CommitTranslatorBundle.message("settings.testConnection.translating")

        // Run network operation in the background thread
        ApplicationManager.getApplication().executeOnPooledThread {
            val service = TranslationService.getInstance()
            val result = service.testConnection(apiUrl, apiKey, model, input)

            // Show result in a UI thread
            ApplicationManager.getApplication().invokeLater({
                testButton?.isEnabled = true
                if (result.isSuccess) {
                    testOutputField.text = result.getOrNull() ?: ""
                } else {
                    val errorMessage = result.exceptionOrNull()?.message
                        ?: CommitTranslatorBundle.message("action.translate.unknownError")
                    testOutputField.text = CommitTranslatorBundle.message(
                        "settings.testConnection.error",
                        errorMessage
                    )
                }
            }, com.intellij.openapi.application.ModalityState.any())
        }
    }

    override fun isModified(): Boolean {
        val settings = CommitTranslatorSettings.getInstance()
        return apiUrlField.text != settings.apiUrl ||
                String(apiKeyField.password) != settings.apiKey ||
                modelField.text != settings.model
    }

    override fun apply() {
        val settings = CommitTranslatorSettings.getInstance()
        settings.apiUrl = apiUrlField.text
        settings.apiKey = String(apiKeyField.password)
        settings.model = modelField.text
    }

    override fun reset() {
        val settings = CommitTranslatorSettings.getInstance()
        apiUrlField.text = settings.apiUrl
        apiKeyField.text = settings.apiKey
        modelField.text = settings.model
    }

    override fun disposeUIResources() {
        panel = null
    }
}
