package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.joincoded.bankapi.NavRoutes
import com.joincoded.bankapi.data.request.CreateUserDTO
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.viewmodel.AuthViewModel
import java.math.BigDecimal
import androidx.compose.material3.TopAppBarDefaults


@Composable
fun RegistrationScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val authMessage by authViewModel.authMessage.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var proceedToKyc by remember { mutableStateOf(false) }

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
    @OptIn(ExperimentalMaterial3Api::class)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = backgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            if (!proceedToKyc) {
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
                            proceedToKyc = true
                        } else {
                            authViewModel.setAuthMessage("Passwords do not match.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Continue", color = primaryDark)
                }

            } else {
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
                    label = { Text("Salary (in KD)", color = Color.White) },
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp)
                )

                Button(
                    onClick = {
                        authViewModel.registerWithKyc(
                            userData = CreateUserDTO(
                                username = username,
                                password = password
                            ),
                            kyc = KYCRequest(
                                firstName = firstName,
                                lastName = lastName,
                                dateOfBirth = dateOfBirth,
                                civilId = civilId,
                                country = country,
                                phoneNumber = phoneNumber,
                                homeAddress = homeAddress,
                                salary = salary.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Register", color = primaryDark)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = authMessage, color = onSurfaceDark, modifier = Modifier.padding(top = 8.dp))

            if (authMessage == "Registration and KYC completed successfully!") {
                LaunchedEffect(Unit) {
                    navController.navigate(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value + "?registered=true")
                }
            }
        }
    }
}
