package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.ui.components.NebulaPasswordField
import com.example.myapplication.ui.components.ValidatedOutlinedField
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaScreenBackground
import com.example.myapplication.util.ApiErrorParser
import com.example.myapplication.util.FormValidators
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    val primaryGreen = NebulaGreen
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var submitAttempted by remember { mutableStateOf(false) }

    val emailError = remember(email, submitAttempted) {
        if (!submitAttempted && email.isBlank()) null else FormValidators.validateEmail(email)
    }
    val passwordError = remember(password, submitAttempted) {
        if (!submitAttempted && password.isBlank()) null else FormValidators.validatePassword(password)
    }
    val confirmPasswordError = remember(password, confirmPassword, submitAttempted) {
        when {
            !submitAttempted && confirmPassword.isBlank() -> null
            confirmPassword.isBlank() -> "Confirma la contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
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
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Crear Cuenta",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Únete a Digital Nebula",
                    color = Color(0xFFCCCCCC),
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {

                Text(
                    text = "CORREO ELECTRÓNICO",
                    color = Color(0xFFB0B0C0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ValidatedOutlinedField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    error = emailError,
                    placeholder = "nombre@nebula.io"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CONTRASEÑA",
                    color = Color(0xFFB0B0C0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                NebulaPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    error = passwordError,
                    placeholder = "Mínimo 6 caracteres"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CONFIRMAR CONTRASEÑA",
                    color = Color(0xFFB0B0C0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                NebulaPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Repetir contraseña",
                    error = confirmPasswordError,
                    placeholder = "Repite la contraseña"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        submitAttempted = true
                        errorMessage = ""
                        successMessage = ""

                        if (emailError != null || passwordError != null || confirmPasswordError != null) {
                            return@Button
                        }

                        val request = LoginRequest(
                            email = email.trim(),
                            password = password
                        )

                        RetrofitClient.api.register(request)
                            .enqueue(object : Callback<Void> {

                                override fun onResponse(
                                    call: Call<Void>,
                                    response: Response<Void>
                                ) {

                                    if (response.isSuccessful) {

                                        successMessage =
                                            "Cuenta creada correctamente"

                                        onRegisterSuccess()

                                    } else {

                                        errorMessage = ApiErrorParser.message(response)
                                    }
                                }

                                override fun onFailure(
                                    call: Call<Void>,
                                    t: Throwable
                                ) {

                                    errorMessage =
                                        "Error de conexión con el servidor"
                                }
                            })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Text(
                        text = "Crear Cuenta",
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

                if (successMessage.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = successMessage,
                        color = primaryGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    "¿Ya tienes una cuenta? ",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp
                )

                Text(
                    "Inicia sesión",
                    color = primaryGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}