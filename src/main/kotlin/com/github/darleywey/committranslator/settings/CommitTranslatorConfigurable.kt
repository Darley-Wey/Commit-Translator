package com.github.darleywey.committranslator.settings

import com.github.darleywey.committranslator.CommitTranslatorBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class CommitTranslatorConfigurable : Configurable {

    private var panel: DialogPanel? = null
    private val apiUrlField = JBTextField()
    private val apiKeyField = JBPasswordField()
    private val modelField = JBTextField()

    override fun getDisplayName(): String = CommitTranslatorBundle.message("settings.displayName")

    override fun createComponent(): JComponent {
        val settings = CommitTranslatorSettings.getInstance()
        
        panel = panel {
            row(CommitTranslatorBundle.message("settings.apiUrl")) {
                cell(apiUrlField)
                    .align(AlignX.FILL)
                    .comment(CommitTranslatorBundle.message("settings.apiUrl.comment"))
            }.layout(RowLayout.PARENT_GRID)
            
            row(CommitTranslatorBundle.message("settings.apiKey")) {
                cell(apiKeyField)
                    .align(AlignX.FILL)
                    .comment(CommitTranslatorBundle.message("settings.apiKey.comment"))
            }.layout(RowLayout.PARENT_GRID)
            
            row(CommitTranslatorBundle.message("settings.model")) {
                cell(modelField)
                    .align(AlignX.FILL)
                    .comment(CommitTranslatorBundle.message("settings.model.comment"))
            }.layout(RowLayout.PARENT_GRID)
        }
        
        apiUrlField.text = settings.apiUrl
        apiKeyField.text = settings.apiKey
        modelField.text = settings.model
        
        return panel!!
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
