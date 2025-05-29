package com.joincoded.bankapi.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSocialClick: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val neonPurple = Color(0xFFB297E7)
    val darkBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1C1E), Color(0xFFA594C7), Color(0xFF1C1C1E))
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = neonPurple,
            background = Color(0xFF1C1C1E),
            surface = Color(0xFF1C1C1E),
            onPrimary = Color.White,
            onBackground = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Welcome Back!",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = neonPurple
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Log in to your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF323232)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email address", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF323232),
                                unfocusedContainerColor = Color(0xFF323232),
                                focusedIndicatorColor = neonPurple,
                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = neonPurple
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color.White) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF323232),
                                unfocusedContainerColor = Color(0xFF323232),
                                focusedIndicatorColor = neonPurple,
                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = neonPurple
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onForgotPasswordClick) {
                            Text("Forgot Password?", color = neonPurple)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onLoginClick(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Sign In", color = neonPurple)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRegisterClick) {
                    Text("Don't have an account? Sign Up", color = neonPurple, textDecoration = TextDecoration.Underline)
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                repeat(3) {
                    IconButton(onClick = { onSocialClick("social$it") }) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }
    }
}
