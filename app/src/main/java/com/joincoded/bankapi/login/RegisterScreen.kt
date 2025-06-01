package com.joincoded.bankapi.login

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationScreen(
    onRegisterClick: (String, String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val neonPurple = Color(0xFFB297E7)
    val darkGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1C1E), Color(0xFF323232), Color(0xFF1C1C1E))
    )
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF323232),
        unfocusedContainerColor = Color(0xFF323232),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = neonPurple
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGradient)
            .padding(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val gradientBrush = Brush.linearGradient(
                colors = listOf(Color(0xFFAA8EE1), Color(0xFF8356C0)),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )

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
            TextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name", color = Color.White) }, colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email", color = Color.White) }, colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Password", color = Color.White) }, visualTransformation = PasswordVisualTransformation(), colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password", color = Color.White) }, visualTransformation = PasswordVisualTransformation(), colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number", color = Color.White) }, colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = nationalId, onValueChange = { nationalId = it }, label = { Text("National ID", color = Color.White) }, colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = address, onValueChange = { address = it }, label = { Text("Address (Optional)", color = Color.White) }, colors = fieldColors, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)), shape = RoundedCornerShape(30.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onRegisterClick(fullName, email, password, confirmPassword, phone, nationalId, address) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Register", color = neonPurple)
            }
        }
    }
}
