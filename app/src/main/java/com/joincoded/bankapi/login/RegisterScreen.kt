package com.joincoded.bankapi.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistrationKycScreen(
    onRegisterClick: (
        String, String, String, String, String,
        String, String, String
    ) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }

    val neonPurple = Color(0xFFB297E7)
    val darkBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1C1E), Color(0xFFA594C7), Color(0xFF1C1C1E))
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = neonPurple,
            onPrimary = Color.White,
            background = Color(0xFF1C1C1E),
            surface = Color(0xFF1C1C1E),
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text("KYC Registration", fontSize = 24.sp, color = neonPurple)
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = dateOfBirth, onValueChange = { dateOfBirth = it }, label = { Text("Date of Birth", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = civilId, onValueChange = { civilId = it }, label = { Text("Civil ID", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = homeAddress, onValueChange = { homeAddress = it }, label = { Text("Home Address", color = Color.White) }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = salary,
                            onValueChange = { salary = it },
                            label = { Text("Salary", color = Color.White) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onRegisterClick(
                                    firstName, lastName, country, dateOfBirth,
                                    civilId, phoneNumber, homeAddress, salary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.8f))
                        ) {
                            Text("Submit KYC", color = neonPurple)
                        }
                    }
                }
            }
        }
    }
}
