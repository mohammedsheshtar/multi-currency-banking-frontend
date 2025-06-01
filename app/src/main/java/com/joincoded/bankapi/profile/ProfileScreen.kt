package com.joincoded.bankapi.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.R

@Composable
fun ProfileScreen(
    onUpdateKYCClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val accentPurple = Color(0xFFB297E7)
    val darkBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E))
    )
    val cardColor = Color(0xFF2C2C2E)
    val textWhite = Color.White
    val pointsColor = Color(0xFF9A6AFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Zainab Al-Saffar",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF9A6AFF), Color(0xFF6B3ACB))
                                            )
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("👑 Gold", color = Color.White)
                                }
                            }
                            Text(" ⭐ 3,200 pts ", color = textWhite, fontWeight = FontWeight.Bold)
                        }

                    }
                    IconButton(
                        onClick = onUpdateKYCClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = accentPurple
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Personal Info Icon",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personal Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textWhite
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoCard("First Name", "Zainab", Modifier.weight(1f))
                    InfoCard("Last Name", "Al-Saffar", Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoCard("Date of Birth", "1998-06-15", Modifier.weight(1f))
                    InfoCard("Civil ID", "282093016000", Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoCard("Country", "Kuwait", Modifier.weight(1f))
                    InfoCard("Phone Number", "+965 5512 1234", Modifier.weight(1f))
                }
                InfoCard("Home Address", "Block 4, Street 12, Hawalli, Kuwait City", Modifier.fillMaxWidth())
                InfoCard("Salary", "1,200 KWD", Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF9A6AFF), Color(0xFF6B3ACB))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.crown),
                                contentDescription = "Crown",
                                tint = Color.Yellow,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Current Tier",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Gold",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFFD700), Color(0xFFDAA520))
                                        )
                                    )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Points",
                                color = pointsColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "3,200",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogoutClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentPurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color(0xFFB297E7), fontSize = 14.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
