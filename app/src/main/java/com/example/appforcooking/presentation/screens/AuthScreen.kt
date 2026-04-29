package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appforcooking.R
import com.example.appforcooking.data.auth.AuthManager
import com.example.appforcooking.presentation.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    val viewModel: AuthViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authManager) as T
            }
        }
    )

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    var passwordMismatchError by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        email = ""
        password = ""
        confirmPassword = ""
        firstName = ""
        lastName = ""
        passwordMismatchError = null
        viewModel.clearError()
    }

    LaunchedEffect(isLoginMode) {
        resetForm()
    }

    LaunchedEffect(navigationEvent) {
        if (navigationEvent) {
            viewModel.consumeNavigationEvent()
            onAuthSuccess()
        }
    }

    LaunchedEffect(password, confirmPassword) {
        if (!isLoginMode) {
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                passwordMismatchError = "Пароли не совпадают"
            } else {
                passwordMismatchError = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Фон",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLoginMode) "Вход" else "Регистрация",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3949AB)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = error != null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        },
                        label = { Text("Пароль") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        isError = error != null
                    )

                    if (!isLoginMode) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                viewModel.clearError()
                            },
                            label = { Text("Подтверждение пароля") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            isError = passwordMismatchError != null,
                            supportingText = {
                                if (passwordMismatchError != null) {
                                    Text(
                                        text = passwordMismatchError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Имя (необязательно)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Фамилия (необязательно)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error!!,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    val isRegisterEnabled = if (!isLoginMode) {
                        email.isNotBlank() &&
                                password.isNotBlank() &&
                                confirmPassword.isNotBlank() &&
                                password == confirmPassword &&
                                !isLoading &&
                                passwordMismatchError == null
                    } else {
                        email.isNotBlank() && password.isNotBlank() && !isLoading
                    }

                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                viewModel.register(email, password, firstName, lastName)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = isRegisterEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading || isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(if (isLoginMode) "Войти" else "Зарегистрироваться", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = {
                        isLoginMode = !isLoginMode
                    }) {
                        Text(
                            if (isLoginMode) "Нет аккаунта? Зарегистрируйтесь" else "Уже есть аккаунт? Войдите",
                            color = Color(0xFF3949AB)
                        )
                    }
                }
            }
        }
    }
}