package com.joincoded.bankapi.composable


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.R

@Composable
fun FilterOptionsButton(
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFB297E7)
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize()) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_list_24),
            contentDescription = "Filter Options",
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF1A1A1D), shape = RoundedCornerShape(8.dp))
        ) {
            listOf(
                "By Date" to "date",
                "By Amount" to "amount",
                "Deposited" to "deposit",
                "Withdrawn" to "withdraw"
            ).forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label, color = Color.White) },
                    onClick = {
                        expanded = false
                        onFilterSelected(value)
                    },
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp)
                )
            }
        }
    }
}
