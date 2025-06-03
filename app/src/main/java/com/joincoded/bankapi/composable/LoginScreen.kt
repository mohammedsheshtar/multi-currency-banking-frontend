package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joincoded.bankapi.NavRoutes
import com.joincoded.bankapi.R
import com.joincoded.bankapi.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSocialClick: (String) -> Unit,
    registered: Boolean = false
) {
    val authViewModel: AuthViewModel = viewModel()
    val authMessage by authViewModel.authMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val token by authViewModel.token.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(isLoggedIn, token) {
        if (isLoggedIn && token != null) {
            navController.navigate("${NavRoutes.NAV_ROUTE_PROFILE_SCREEN.value}/$token") {
                popUpTo(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value) { inclusive = true }
            }
        }
    }

    val backgroundDark = Color(0xFF141219)
    val primaryDark = Color(0xFFCDBDFF)
    val onSurfaceDark = Color(0xFFE6E0EA)
    val fieldBackground = Color(0xFF323232)

    val gradientBrush = Brush.linearGradient(
        colors = listOf(primaryDark, Color(0xFF8A2BE2)),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = fieldBackground,
        unfocusedContainerColor = fieldBackground,
        focusedIndicatorColor = primaryDark,
        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
        cursorColor = primaryDark
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                BasicText(
                    text = "Welcome Back!",
                    style = TextStyle(
                        brush = gradientBrush,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Log in to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceDark
            )
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp)
        ) {
            if (registered) {
                Text(
                    text = "🎉 Registration Successful!",
                    color = Color(0xFF8BC34A),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = fieldBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp)),
                        colors = fieldColors,
                        shape = RoundedCornerShape(30.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.White) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp)),
                        colors = fieldColors,
                        shape = RoundedCornerShape(30.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onForgotPasswordClick) {
                        Text("Forgot Password?", color = primaryDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.login(username, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Sign In", color = primaryDark)
            }

            Text(
                text = authMessage,
                color = onSurfaceDark,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRegisterClick) {
                Text(
                    "Don't have an account? Sign Up",
                    color = primaryDark,
                    textDecoration = TextDecoration.Underline
                )
            }
        }


        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onSocialClick("google") },
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.google), contentDescription = "Google Sign-In", tint = Color.Unspecified, modifier = Modifier.size(25.dp))
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onSocialClick("email") },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Email, contentDescription = "Email Sign-In", tint = Color.White, modifier = Modifier.size(25.dp))
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onSocialClick("phone") },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Phone, contentDescription = "Phone Sign-In", tint = Color.White, modifier = Modifier.size(25.dp))
            }
        }
    }
}
