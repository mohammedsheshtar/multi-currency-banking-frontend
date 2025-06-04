package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joincoded.bankapi.R
import com.joincoded.bankapi.data.response.KYCResponse
import com.joincoded.bankapi.viewmodel.KycViewModel
import com.joincoded.bankapi.viewmodel.KycUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: KycViewModel,
    onLogout: () -> Unit = {
        // Default implementation that navigates to login screen
        navController.navigate("login") {
            popUpTo("profile") { inclusive = true }
        }
    }
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Initialize the view model with context only
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    // Fetch KYC data only after initialization
    LaunchedEffect(viewModel) {
        if (viewModel.isInitialized()) {
            viewModel.fetchKycData()
        }
    }
    
    val kycUiState by viewModel.kycUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    val backgroundDark = Color(0xFF141219)
    val primaryDark = Color(0xFFCDBDFF)
    val onSurfaceDark = Color(0xFFE6E0EA)
    val fieldBackground = Color(0xFF323232)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    "Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                // Empty box for balance
                Box(modifier = Modifier.size(48.dp))
            }
            
            when (kycUiState) {
                is KycUiState.Success -> {
                    val kycData = (kycUiState as KycUiState.Success).data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = fieldBackground
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Profile Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(60.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFAA8EE1),
                                                    Color(0xFF8356C0)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = kycData?.firstName?.firstOrNull()?.toString() ?: "?",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            kycData?.let { kyc ->
                                ProfileField("First Name", kyc.firstName)
                                ProfileField("Last Name", kyc.lastName)
                                ProfileField("Date of Birth", kyc.dateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                ProfileField("Civil ID", kyc.civilId)
                                ProfileField("Country", kyc.country)
                                ProfileField("Phone Number", kyc.phoneNumber)
                                ProfileField("Email", kyc.email)
                            }
                            
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { viewModel.toggleEditing() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryDark
                                    ),
                                    shape = RoundedCornerShape(30.dp)
                                ) {
                                    Text(
                                        if (isEditing) "Save Changes" else "Edit Profile",
                                        color = Color.Black
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Button(
                                    onClick = onLogout,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE57373)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(30.dp)
                                ) {
                                    Text("Logout", color = Color.White)
                                }
                            }
                        }
                    }
                }
                
                is KycUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = fieldBackground
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_error_outline_24),
                                contentDescription = "Error",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = (kycUiState as KycUiState.Error).message,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { viewModel.fetchKycData() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryDark
                                ),
                                shape = RoundedCornerShape(30.dp)
                            ) {
                                Text("Retry", color = Color.Black)
                            }
                        }
                    }
                }
                
                KycUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryDark,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB297E7)
                )
            ) {
                Text(
                    "Logout",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = primaryDark,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Divider(
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
