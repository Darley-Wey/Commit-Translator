package com.github.darleywey.committranslator.actions

import com.github.darleywey.committranslator.CommitTranslatorBundle
import com.github.darleywey.committranslator.services.TranslationService
import com.github.darleywey.committranslator.settings.CommitTranslatorSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.AnimatedIcon

class TranslateCommitMessageAction : AnAction() {

    @Volatile
    private var isTranslating = false

    private val commitMessageControl = DataKey.create<Any>("Vcs.CommitMessage.Control")
    private val checkinPanel = DataKey.create<Any>("Vcs.CheckinProjectPanel")
    private val refreshablePanel = DataKey.create<Any>("RefreshablePanel")
    private val workflowHandler = DataKey.create<Any>("Vcs.CommitWorkflowHandler")

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitMessage = findCommitMessage(e) ?: return

        if (isTranslating) return

        val originalText = extractText(commitMessage)
        if (originalText.isNullOrBlank()) {
            Messages.showWarningDialog(
                project,
                CommitTranslatorBundle.message("action.translate.emptyMessage"),
                CommitTranslatorBundle.message("action.translate.title")
            )
            return
        }

        val settings = CommitTranslatorSettings.getInstance()
        if (settings.apiKey.isBlank()) {
            val result = Messages.showYesNoDialog(
                project,
                CommitTranslatorBundle.message("action.translate.noApiKey"),
                CommitTranslatorBundle.message("action.translate.title"),
                CommitTranslatorBundle.message("action.translate.openSettings"),
                CommitTranslatorBundle.message("action.translate.cancel"),
                Messages.getWarningIcon()
            )
            if (result == Messages.YES) {
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, CommitTranslatorBundle.message("settings.displayName"))
            }
            return
        }

        isTranslating = true

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            CommitTranslatorBundle.message("action.translate.progress"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                val result = TranslationService.getInstance().translateToEnglish(originalText)

                ApplicationManager.getApplication().invokeLater {
                    isTranslating = false
                    result.fold(
                        onSuccess = { translatedText ->
                            updateCommitMessage(commitMessage, translatedText)
                        },
                        onFailure = { error ->
                            val errorMessage: String = CommitTranslatorBundle.message("action.translate.error", error.message ?: CommitTranslatorBundle.message("action.translate.unknownError"))
                            val title: String = CommitTranslatorBundle.message("action.translate.title")
                            Messages.showErrorDialog(project, errorMessage, title)
                        }
                    )
                }
            }

            override fun onCancel() {
                isTranslating = false
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val commitMessage = findCommitMessage(e)
        e.presentation.isEnabledAndVisible = commitMessage != null
        e.presentation.isEnabled = !isTranslating

        if (isTranslating) {
            e.presentation.icon = AnimatedIcon.Default.INSTANCE
        } else {
            e.presentation.icon = IconLoader.getIcon("/icons/translate.svg", javaClass)
        }
    }

    private fun extractText(commitMessage: Any): String? {
        try {
            val methods = commitMessage.javaClass.methods

            // 1. 尝试 getCommitMessage()
            val getCommitMsgMethod = methods.find { it.name == "getCommitMessage" && it.parameterCount == 0 }
            val commitMsgObj = getCommitMsgMethod?.invoke(commitMessage)

            if (commitMsgObj is String) return commitMsgObj

            if (commitMsgObj != null) {
                val innerMethods = commitMsgObj.javaClass.methods
                innerMethods.find { it.name == "getText" || it.name == "getComment" }?.let {
                    return it.invoke(commitMsgObj) as? String
                }
            }

            // 2. 尝试从 getCommitMessageUi() 获取
            methods.find { it.name == "getCommitMessageUi" }?.invoke(commitMessage)?.let { uiObj ->
                uiObj.javaClass.methods.find { it.name == "getText" || it.name == "getComment" }?.let {
                    return it.invoke(uiObj) as? String
                }
            }

            // 3. 通用回退
            methods.find { it.name == "getText" || it.name == "getComment" }?.let {
                return it.invoke(commitMessage) as? String
            }
        } catch (_: Exception) {}
        return null
    }

    private fun updateCommitMessage(commitMessage: Any, text: String) {
        try {
            val methods = commitMessage.javaClass.methods

            // 1. 尝试直接 setCommitMessage
            val setMethod = methods.find {
                it.name == "setCommitMessage" && it.parameterCount == 1 && it.parameterTypes[0] == String::class.java
            }

            if (setMethod != null) {
                setMethod.invoke(commitMessage, text)
            } else {
                // 2. 尝试获取 UI 并设置
                val getUiMethod = methods.find { it.name == "getCommitMessageUi" || it.name == "getCommitMessage" }
                val uiObj = getUiMethod?.invoke(commitMessage)
                if (uiObj != null && uiObj !is String) {
                    uiObj.javaClass.methods.find {
                        (it.name == "setText" || it.name == "setCommitMessage") && it.parameterCount == 1
                    }?.invoke(uiObj, text)
                }
            }
        } catch (_: Exception) {}
    }

    private fun findCommitMessage(e: AnActionEvent): Any? {
        e.getData(commitMessageControl)?.let { return it }

        e.getData(workflowHandler)?.let { handler ->
            try {
                handler.javaClass.methods.find { it.name == "getUi" }?.invoke(handler)?.let { return it }
            } catch (_: Exception) {}
        }

        e.getData(checkinPanel)?.let { return it }
        e.getData(refreshablePanel)?.let { return it }

        var component = e.getData(com.intellij.openapi.actionSystem.PlatformCoreDataKeys.CONTEXT_COMPONENT)
        while (component != null) {
            val methods = component.javaClass.methods
            if (methods.any { it.name == "setCommitMessage" || it.name == "setComment" }) {
                return component
            }
            if (methods.any { it.name == "getEditorField" }) {
                try {
                    methods.find { it.name == "getEditorField" }?.invoke(component)?.let { return it }
                } catch (_: Exception) {}
            }
            component = component.parent
        }
        return null
    }
}
