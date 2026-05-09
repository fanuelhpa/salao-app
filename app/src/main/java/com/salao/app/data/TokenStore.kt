package com.salao.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salao_prefs")

// Representa os três estados possíveis do token
sealed class TokenState {
    object Carregando : TokenState()           // ainda lendo o DataStore
    object SemToken : TokenState()             // leu e não tem token
    data class ComToken(val token: String) : TokenState()  // leu e tem token
}

class TokenStore(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    val tokenState: Flow<TokenState> = context.dataStore.data
        .map { preferences ->
            val token = preferences[TOKEN_KEY]
            if (token.isNullOrBlank()) {
                TokenState.SemToken
            } else {
                TokenState.ComToken(token)
            }
        }

    suspend fun salvarToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun limparToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}