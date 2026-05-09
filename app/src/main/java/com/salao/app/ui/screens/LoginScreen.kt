package com.salao.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salao.app.ui.theme.*
import com.salao.app.viewmodel.AuthUiState
import com.salao.app.viewmodel.AuthViewModel

// @Composable marca essa função como uma função de UI do Jetpack Compose
// Funções @Composable só podem ser chamadas dentro de outras funções @Composable
// Parâmetros:
//   viewModel - gerencia o estado da tela (loading, erro, sucesso)
//   onLoginSuccess - função chamada quando o login funcionar, passando o token
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit
) {
    // remember { } mantém o valor na memória enquanto a tela estiver ativa
    // mutableStateOf("") cria um estado observável — quando muda, a tela redesenha
    // "by" é um delegate do Kotlin que simplifica .value na leitura e escrita
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    // collectAsState() observa o StateFlow do ViewModel
    // Toda vez que o uiState mudar, essa linha dispara e a tela redesenha
    val uiState by viewModel.uiState.collectAsState()

    // LaunchedEffect executa um bloco de código quando o valor entre chaves mudar
    // Aqui: sempre que uiState mudar, verifica se é Success para navegar
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            // Chama a função de navegação passando o token recebido da API
            onLoginSuccess((uiState as AuthUiState.Success).token)
        }
    }

    // Column organiza os filhos verticalmente (um embaixo do outro)
    // Modifier encadeia modificações visuais e de comportamento
    // fillMaxSize() = ocupa toda a tela
    // background() = define a cor de fundo
    // padding() = espaçamento interno
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdeSurface)
            .padding(32.dp),
        // Arrangement.Center centraliza os filhos verticalmente
        verticalArrangement = Arrangement.Center,
        // Alignment.CenterHorizontally centraliza horizontalmente
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Box empilha elementos um sobre o outro (como um FrameLayout no XML)
        // Aqui usamos para criar o círculo com o ícone de tesoura no centro
        Box(
            modifier = Modifier
                .size(72.dp)           // tamanho fixo 72x72
                .clip(CircleShape)     // recorta em formato circular
                .background(VerdeFundo), // cor de fundo do círculo
            contentAlignment = Alignment.Center // centraliza o conteúdo dentro do Box
        ) {
            Text(text = "✂", fontSize = 32.sp)
        }

        // Spacer adiciona espaço vazio entre elementos
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Salao de Beleza",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeMusgo
        )

        Text(
            text = "Bem-vinda de volta",
            fontSize = 14.sp,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // OutlinedTextField é o campo de texto com borda do Material3
        // value = o valor atual do campo
        // onValueChange = função chamada a cada caractere digitado
        // label = texto flutuante acima do campo quando em foco
        // singleLine = impede quebra de linha
        // shape = formato das bordas (arredondado)
        // colors = personaliza as cores do campo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it }, // "it" é o novo valor digitado
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            // PasswordVisualTransformation substitui os caracteres por "••••"
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeMusgo,
                focusedLabelColor = VerdeMusgo,
                cursorColor = VerdeMusgo
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Exibe a mensagem de erro somente se o estado for AuthUiState.Error
        // "is" verifica o tipo do objeto (como instanceof no Java)
        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Button — botão principal da tela
        // onClick = função executada ao clicar
        // enabled = false desativa o botão (enquanto está carregando)
        Button(
            onClick = { viewModel.login(email, senha) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeMusgo),
            enabled = uiState !is AuthUiState.Loading
        ) {
            // Mostra spinner de carregamento OU texto dependendo do estado
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Branco,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}