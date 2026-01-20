package com.github.darleywey.committranslator.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "CommitTranslatorSettings",
    storages = [Storage("CommitTranslatorSettings.xml")]
)
class CommitTranslatorSettings : PersistentStateComponent<CommitTranslatorSettings.State> {

    data class State(
        var apiUrl: String = "https://api.openai.com/v1/chat/completions",
        var model: String = "gpt-4o-mini"
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var apiUrl: String
        get() = myState.apiUrl
        set(value) {
            myState.apiUrl = value
        }

    var model: String
        get() = myState.model
        set(value) {
            myState.model = value
        }

    var apiKey: String
        get() = PasswordSafe.instance.getPassword(credentialAttributes) ?: ""
        set(value) {
            PasswordSafe.instance.set(credentialAttributes, Credentials("", value))
        }

    private val credentialAttributes: CredentialAttributes
        get() = CredentialAttributes(
            generateServiceName("CommitTranslator", "apiKey")
        )

    companion object {
        fun getInstance(): CommitTranslatorSettings {
            return ApplicationManager.getApplication().getService(CommitTranslatorSettings::class.java)
        }
    }
}
