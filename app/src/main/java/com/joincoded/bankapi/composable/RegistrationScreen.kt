package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joincoded.bankapi.NavRoutes
import com.joincoded.bankapi.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val authMessage by authViewModel.authMessage.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val backgroundDark = Color(0xFF141219)
    val primaryDark = Color(0xFFCDBDFF)
    val onSurfaceDark = Color(0xFFE6E0EA)
    val fieldBackground = Color(0xFF323232)

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFAA8EE1), Color(0xFF8356C0)),
        start = Offset(0f, 0f),
        end = Offset(400f, 400f)
    )
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = fieldBackground,
        unfocusedContainerColor = fieldBackground,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = primaryDark
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                text = "Create your account!",
                style = TextStyle(
                    brush = gradientBrush,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name", color = Color.White) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color.White) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )
            TextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number", color = Color.White) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )
            TextField(
                value = nationalId,
                onValueChange = { nationalId = it },
                label = { Text("National ID", color = Color.White) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )
            TextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Optional)", color = Color.White) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).padding(vertical = 4.dp),
                shape = RoundedCornerShape(30.dp)
            )

            Button(
                onClick = {
                    if (password == confirmPassword) {
                        authViewModel.register(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Register", color = primaryDark)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = authMessage,
                color = onSurfaceDark,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (authMessage == "Registration successful!") {
                LaunchedEffect(Unit) {
                    navController.navigate(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value)
                }
            }
        }
    }
}
