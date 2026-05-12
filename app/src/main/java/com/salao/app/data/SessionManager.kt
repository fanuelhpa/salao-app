package com.salao.app.data

import android.content.Context

// Objeto único que limpa o token quando a sessão expira
object SessionManager {
    private var tokenStore: TokenStore? = null

    fun init(context: Context) {
        if (tokenStore == null) {
            tokenStore = TokenStore(context.applicationContext)
        }
    }

    suspend fun limparSessao() {
        tokenStore?.limparToken()
    }
}