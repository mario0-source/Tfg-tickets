package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.auth.SessionManager
import com.example.myapplication.ui.components.NebulaLoginPasswordField
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaScreenBackground
import com.example.myapplication.ui.theme.NebulaTextMuted
import com.example.myapplication.ui.theme.NebulaTextSecondary
import com.example.myapplication.util.ApiErrorParser
import com.example.myapplication.util.FormValidators


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun DigitalNebulaLoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
){

    val primaryGreen = NebulaGreen
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitAttempted by remember { mutableStateOf(false) }

    val emailError = remember(email, submitAttempted) {
        if (!submitAttempted && email.isBlank()) null else FormValidators.validateEmail(email)
    }
    val passwordError = remember(password, submitAttempted) {
        if (!submitAttempted && password.isBlank()) null else FormValidators.validatePassword(password)
    }

    NebulaScreenBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Cloud",
                        tint = primaryGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Digital Nebula",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "¡Bienvenido de nuevo!",
                    color = NebulaTextMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column {

                // Email
                Text(
                    text = "CORREO ELECTRÓNICO",
                    color = NebulaTextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("nombre@nebula.io", color = Color(0xFF9A9A9A)) },
                    singleLine = true,
                    isError = emailError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        errorBorderColor = Color(0xFFFF8080),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = primaryGreen
                    )
                )
                emailError?.let {
                    Text(it, color = Color(0xFFFF8080), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CONTRASEÑA",
                        color = NebulaTextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "¿OLVIDASTE?",
                        color = primaryGreen,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                NebulaLoginPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                )
                passwordError?.let {
                    Text(it, color = Color(0xFFFF8080), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        submitAttempted = true
                        errorMessage = ""

                        if (emailError != null || passwordError != null) {
                            return@Button
                        }

                        val request = LoginRequest(
                            email = email.trim(),
                            password = password
                        )

                        RetrofitClient.api.login(request)
                            .enqueue(object : Callback<LoginResponse> {

                                override fun onResponse(
                                    call: Call<LoginResponse>,
                                    response: Response<LoginResponse>
                                ) {

                                    if (response.isSuccessful) {

                                        val token = response.body()?.token

                                        val sessionManager = SessionManager(context)

                                        sessionManager.saveToken(token)
                                        sessionManager.saveEmail(email)

                                        RetrofitClient.setToken(token)

                                        Log.d("LOGIN", "TOKEN: $token")

                                        if (!token.isNullOrEmpty()) {
                                            onLoginSuccess(token)
                                        }

                                    } else {
                                        errorMessage = ApiErrorParser.message(response)
                                    }
                                }

                                override fun onFailure(
                                    call: Call<LoginResponse>,
                                    t: Throwable
                                ) {

                                    Log.e("API_ERROR", t.message.toString())

                                    errorMessage = t.message.toString()
                                }
                            })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Text(
                        text = "Iniciar Sesión",
                        color = Color(0xFF020208),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (errorMessage.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.weight(1f))
                    Text(
                        text = "  O CONTINUAR CON  ",
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp
                    )
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Google", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Apple", color = Color.White)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿No tienes una cuenta? ", color = Color(0xFFCCCCCC), fontSize = 13.sp)
                Text(
                    "Crear una cuenta",
                    color = primaryGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        onNavigateToRegister()
                    }
                )
            }
        }
    }
}
