import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.viewmodel.KycViewModel
import com.joincoded.bankapi.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(token: String, onLogout: () -> Unit) {
    val kycViewModel: KycViewModel = viewModel()
    val kycData by kycViewModel.kycData.collectAsState()
    val errorMessage by kycViewModel.errorMessage.collectAsState()

    LaunchedEffect(token) { kycViewModel.fetchKYC(token) }

    val backgroundDark = Color(0xFF141219)
    val surfaceDark = Color(0xFF141219)
    val primaryDark = Color(0xFFCDBDFF)
    val onPrimaryDark = Color(0xFF351A7E)
    val onSurfaceDark = Color(0xFFE6E0EA)
    val errorDark = Color(0xFFFFB4AB)

    val tierGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFB297E7), Color(0xFF8A2BE2)))

    val tierThresholds = listOf("BRONZE" to 0, "SILVER" to 1000, "GOLD" to 2000, "PLATINUM" to 3000, "DIAMOND" to 4000)

    var isEditing by remember { mutableStateOf(false) }
    var editedCountry by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }
    var editedSalary by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedFirstName by remember { mutableStateOf("") }
    var editedLastName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(backgroundDark)) {
        when {
            kycData != null -> {
                if (!isEditing) {
                    editedFirstName = kycData!!.firstName
                    editedLastName = kycData!!.lastName
                    editedCountry = kycData!!.country
                    editedAddress = kycData!!.homeAddress
                    editedSalary = kycData!!.salary.toString()
                    editedPhone = kycData!!.phoneNumber
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = surfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Text("${kycData!!.firstName} ${kycData!!.lastName}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryDark)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(end = 8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .background(tierGradient)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.crown),
                                                    contentDescription = "Tier",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(kycData!!.tier, color = Color.White)
                                            }
                                        }
                                    }
                                    Text(" ⭐ ${kycData!!.points} pts ", color = onSurfaceDark, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { isEditing = !isEditing }, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = primaryDark)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoCard("Date of Birth", kycData!!.dateOfBirth.toString(), surfaceDark, primaryDark, onSurfaceDark, Modifier.weight(1f))
                        InfoCard("Civil ID", kycData!!.civilId, surfaceDark, primaryDark, onSurfaceDark, Modifier.weight(1f))
                    }
                    if (isEditing) {
                        EditableField("First Name", editedFirstName, onSurfaceDark) { editedFirstName = it }
                        EditableField("Last Name", editedLastName, onSurfaceDark) { editedLastName = it }
                        EditableField("Country", editedCountry, onSurfaceDark) { editedCountry = it }
                        EditableField("Phone Number", editedPhone, onSurfaceDark) { editedPhone = it }
                        EditableField("Home Address", editedAddress, onSurfaceDark) { editedAddress = it }
                        EditableField("Salary", editedSalary, onSurfaceDark) { editedSalary = it }
                        Button(onClick = {
                            val salaryValue = editedSalary.toDoubleOrNull() ?: 0.0
                            kycViewModel.updateKYC(token, editedFirstName, editedLastName, editedCountry, editedPhone, editedAddress, salaryValue)
                            kycViewModel.fetchKYC(token)
                            isEditing = false

                        }, colors = ButtonDefaults.buttonColors(containerColor = primaryDark)) {
                            Text("Save", color = onPrimaryDark)
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoCard("Country", kycData!!.country, surfaceDark, primaryDark, onSurfaceDark, Modifier.weight(1f))
                            InfoCard("Phone Number", kycData!!.phoneNumber, surfaceDark, primaryDark, onSurfaceDark, Modifier.weight(1f))
                        }
                        InfoCard("Home Address", kycData!!.homeAddress, surfaceDark, primaryDark, onSurfaceDark)
                        InfoCard("Salary", "${kycData!!.salary} KWD", surfaceDark, primaryDark, onSurfaceDark)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val currentPoints = kycData!!.points
                    val nextTier = tierThresholds.firstOrNull { currentPoints < it.second }
                    val nextTierName = nextTier?.first ?: "MAX"
                    val nextTierPoints = nextTier?.second ?: currentPoints
                    val progress = if (nextTierPoints > 0) currentPoints.toFloat() / nextTierPoints else 1f
                    LinearProgressIndicator(progress = progress, color = primaryDark, modifier = Modifier.fillMaxWidth().height(10.dp))
                    Text("$currentPoints points - Next tier: $nextTierName at $nextTierPoints points", color = onSurfaceDark, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = {
                        kycViewModel.logout()
                        onLogout()
                    }, colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryDark), modifier = Modifier.fillMaxWidth()) {
                        Text("Logout", fontSize = 18.sp)
                    }
                }
            }
            errorMessage.isNotEmpty() -> {
                Text("Error: $errorMessage", color = errorDark, modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryDark)
            }
        }
    }
}


@Composable
fun InfoCard(title: String, value: String, surfaceColor: Color, primaryColor: Color, onSurfaceColor: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = surfaceColor), shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = primaryColor, fontSize = 14.sp)
            Text(value, color = onSurfaceColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EditableField(label: String, value: String, onSurfaceColor: Color, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = onSurfaceColor) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        textStyle = TextStyle(color = onSurfaceColor)
    )
}
