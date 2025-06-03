package com.joincoded.bankapi.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.R

enum class TransactionFilter {
    ALL,
    DEPOSITS,
    WITHDRAWALS,
    TRANSFERS,
    RECENT,
    OLDEST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT;

    fun getLabel(): String {
        return when (this) {
            ALL -> "All Transactions"
            DEPOSITS -> "Deposits Only"
            WITHDRAWALS -> "Withdrawals Only"
            TRANSFERS -> "Transfers Only"
            RECENT -> "Most Recent"
            OLDEST -> "Oldest First"
            HIGHEST_AMOUNT -> "Highest Amount"
            LOWEST_AMOUNT -> "Lowest Amount"
        }
    }
}

@Composable
fun FilterOptionsButton(
    currentFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFB297E7)
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize()) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A1D))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_filter_list_24),
                contentDescription = "Filter Options",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = currentFilter.getLabel(),
                color = tint,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                contentDescription = "Show Filters",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF1A1A1D), shape = RoundedCornerShape(8.dp))
        ) {
            TransactionFilter.values().forEach { filter ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            filter.getLabel(),
                            color = if (filter == currentFilter) Color(0xFFB297E7) else Color.White
                        )
                    },
                    onClick = {
                        expanded = false
                        onFilterSelected(filter)
                    },
                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp),
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White,
                        leadingIconColor = Color.White,
                        trailingIconColor = Color.White,
                        disabledTextColor = Color.Gray,
                        disabledLeadingIconColor = Color.Gray,
                        disabledTrailingIconColor = Color.Gray
                    )
                )
            }
        }
    }
}
