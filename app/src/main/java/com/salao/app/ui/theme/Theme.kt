package com.salao.app.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ColorScheme = lightColorScheme(
    primary = VerdeMusgo,
    onPrimary = Branco,
    primaryContainer = VerdeFundo,
    onPrimaryContainer = TextoPrimario,
    secondary = VerdeMusgoClaro,
    onSecondary = Branco,
    background = VerdeSurface,
    onBackground = TextoPrimario,
    surface = Branco,
    onSurface = TextoPrimario,
    onSurfaceVariant = TextoSecundario,
)

@Composable
fun SalaoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}