package com.joincoded.bankapi.composable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.viewmodel.AuthViewModel
import java.math.BigDecimal
import java.time.LocalDate
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    context: Context = LocalContext.current
) {
    var currentStep by remember { mutableStateOf(1) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }

    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginClick()
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = backgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BasicText(
                text = "Create your account!",
                style = TextStyle(brush = gradientBrush, fontSize = 26.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (currentStep == 1) {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )

                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            currentStep = 2
                        } else {
                            // Show error message
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Next", color = primaryDark)
                }
            } else if (currentStep == 2) {
                TextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = civilId,
                    onValueChange = { civilId = it },
                    label = { Text("Civil ID", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = homeAddress,
                    onValueChange = { homeAddress = it },
                    label = { Text("Home Address", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )
                TextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )

                Button(
                    onClick = {
                        val createUserDTO = CreateUserDTO(username, password)
                        val kycRequest = KYCRequest(
                            firstName = firstName,
                            lastName = lastName,
                            dateOfBirth = dateOfBirth,
                            civilId = civilId,
                            country = country,
                            phoneNumber = phoneNumber,
                            homeAddress = homeAddress,
                            salary = salary.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        )
                        // TODO: Implement register function in AuthViewModel
                        // authViewModel.register(createUserDTO, kycRequest)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Register")
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
