package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.viewmodel.KycViewModel

@Composable
fun ProfileScreen(token: String) {
    val kycViewModel: KycViewModel = viewModel()
    val kycData by kycViewModel.kycData.collectAsState()
    val errorMessage by kycViewModel.errorMessage.collectAsState()

    LaunchedEffect(token) {
        kycViewModel.fetchKYC(token)
    }

    val accentPurple = Color(0xFFB297E7)
    val darkBackground = Brush.verticalGradient(colors = listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E)))
    val pointsColor = Color(0xFF9A6AFF)

    val tierThresholds = listOf("BRONZE" to 0, "SILVER" to 1000, "GOLD" to 2000, "PLATINUM" to 3000, "DIAMOND" to 4000)

    var isEditing by remember { mutableStateOf(false) }
    var editedCountry by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }
    var editedSalary by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(darkBackground)) {
        when {
            kycData != null -> {
                if (!isEditing) {
                    editedCountry = kycData!!.country
                    editedAddress = kycData!!.homeAddress
                    editedSalary = kycData!!.salary.toString()
                    editedPhone = kycData!!.phoneNumber
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Top card with name, tier, points, edit
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Text("${kycData!!.firstName} ${kycData!!.lastName}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accentPurple)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(end = 8.dp)) {
                                        Box(modifier = Modifier.background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF9A6AFF), Color(0xFF6B3ACB)))).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                            Text("👑 ${kycData!!.tier}", color = Color.White)
                                        }
                                    }
                                    Text(" ⭐ ${kycData!!.points} pts ", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { isEditing = !isEditing }, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = accentPurple)
                            }
                        }
                    }

                    // Info fields and editable fields
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoCard("Date of Birth", kycData!!.dateOfBirth.toString(), Modifier.weight(1f))
                        InfoCard("Civil ID", kycData!!.civilId, Modifier.weight(1f))
                    }
                    if (isEditing) {
                        EditableField("Country", editedCountry) { editedCountry = it }
                        EditableField("Phone Number", editedPhone) { editedPhone = it }
                        EditableField("Home Address", editedAddress) { editedAddress = it }
                        EditableField("Salary", editedSalary) { editedSalary = it }
                        Button(onClick = {
                            // TODO: updateKYC logic in ViewModel
                            isEditing = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = accentPurple)) {
                            Text("Save")
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoCard("Country", kycData!!.country, Modifier.weight(1f))
                            InfoCard("Phone Number", kycData!!.phoneNumber, Modifier.weight(1f))
                        }
                        InfoCard("Home Address", kycData!!.homeAddress)
                        InfoCard("Salary", "${kycData!!.salary} KWD")
                    }

                    // Tier progress bar
                    Spacer(modifier = Modifier.height(16.dp))
                    val currentPoints = kycData!!.points
                    val nextTier = tierThresholds.firstOrNull { currentPoints < it.second }
                    val nextTierName = nextTier?.first ?: "MAX"
                    val nextTierPoints = nextTier?.second ?: currentPoints
                    val progress = if (nextTierPoints > 0) currentPoints.toFloat() / nextTierPoints else 1f
                    LinearProgressIndicator(progress = progress, color = accentPurple, modifier = Modifier.fillMaxWidth().height(10.dp))
                    Text("$currentPoints points - Next tier: $nextTierName at $nextTierPoints points", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { /* TODO: logout logic */ }, colors = ButtonDefaults.outlinedButtonColors(contentColor = accentPurple), modifier = Modifier.fillMaxWidth()) {
                        Text("Logout", fontSize = 18.sp)
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Text("Error: $errorMessage", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)), shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color(0xFFB297E7), fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EditableField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        textStyle = TextStyle(color = Color.White)
    )
}
