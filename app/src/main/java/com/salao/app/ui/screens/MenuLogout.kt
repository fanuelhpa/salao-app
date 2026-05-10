package com.salao.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box

@Composable
fun MenuLogout(onLogout: () -> Unit) {

    var menuAberto by remember { mutableStateOf(false) }
    var mostrarConfirmacao by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuAberto = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }
        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sair") },
                onClick = {
                    menuAberto = false
                    mostrarConfirmacao = true
                }
            )
        }
    }

    // Dialog de confirmacao de logout
    if (mostrarConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacao = false },
            containerColor = androidx.compose.ui.graphics.Color.White,
            title = {
                Text(
                    text = "Sair do aplicativo",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text("Tem certeza que deseja sair?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacao = false
                        onLogout()
                    }
                ) {
                    Text("Sair", color = com.salao.app.ui.theme.VerdeMusgo)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacao = false }) {
                    Text("Cancelar", color = com.salao.app.ui.theme.TextoSecundario)
                }
            }
        )
    }
}