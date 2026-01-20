package com.github.darleywey.committranslator.actions

import com.github.darleywey.committranslator.CommitTranslatorBundle
import com.github.darleywey.committranslator.services.TranslationService
import com.github.darleywey.committranslator.settings.CommitTranslatorSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage

class TranslateCommitMessageAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitMessage = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as? CommitMessage ?: return
        
        val originalText = commitMessage.text.trim()
        if (originalText.isEmpty()) {
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

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            CommitTranslatorBundle.message("action.translate.progress"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                
                val result = TranslationService.getInstance().translateToEnglish(originalText)
                
                ApplicationManager.getApplication().invokeLater {
                    result.fold(
                        onSuccess = { translatedText ->
                            commitMessage.setCommitMessage(translatedText)
                        },
                        onFailure = { error ->
                            Messages.showErrorDialog(
                                project,
                                CommitTranslatorBundle.message("action.translate.error", error.message ?: "Unknown error"),
                                CommitTranslatorBundle.message("action.translate.title")
                            )
                        }
                    )
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val commitMessage = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
        e.presentation.isEnabledAndVisible = commitMessage != null
    }
}
