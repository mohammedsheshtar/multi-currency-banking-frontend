package com.joincoded.bankapi.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationKycScreen(
    onRegisterClick: (String, String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(320.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Register and KYC", fontSize = 24.sp, color = Color(0xFFB297E7), modifier = Modifier.padding(bottom = 16.dp))

                val fieldModifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                val fieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF323232),
                    unfocusedContainerColor = Color(0xFF323232),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    textColor = Color.White
                )

                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, placeholder = { Text("Full Name", color = Color.White.copy(alpha = 0.6f)) }, colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)
                OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("Email", color = Color.White.copy(alpha = 0.6f)) }, colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)
                OutlinedTextField(value = password, onValueChange = { password = it }, placeholder = { Text("Password", color = Color.White.copy(alpha = 0.6f)) }, visualTransformation = PasswordVisualTransformation(), colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = { Text("Confirm Password", color = Color.White.copy(alpha = 0.6f)) }, visualTransformation = PasswordVisualTransformation(), colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = { Text("Phone Number", color = Color.White.copy(alpha = 0.6f)) }, colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)
                OutlinedTextField(value = nationalId, onValueChange = { nationalId = it }, placeholder = { Text("National ID", color = Color.White.copy(alpha = 0.6f)) }, colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)
                OutlinedTextField(value = address, onValueChange = { address = it }, placeholder = { Text("Address (Optional)", color = Color.White.copy(alpha = 0.6f)) }, colors = fieldColors, shape = RoundedCornerShape(24.dp), modifier = fieldModifier)

                Button(
                    onClick = { onRegisterClick(fullName, email, password, confirmPassword, phoneNumber, nationalId, address) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF411F)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Register", color = Color.White)
                }
            }
        }
    }
}
