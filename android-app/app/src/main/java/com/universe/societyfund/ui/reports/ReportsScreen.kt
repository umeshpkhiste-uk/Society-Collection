package com.universe.societyfund.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.universe.societyfund.model.Receipt

@Composable
fun ReportsScreen(receipts: List<Receipt>) {
    var query by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf("") }

    val filtered = remember(query, dateFilter, receipts) {
        receipts.filter { r ->
            val matchesFlatOrName = query.isBlank() ||
                r.flatNumber.contains(query, ignoreCase = true) ||
                r.residentName.contains(query, ignoreCase = true) ||
                r.receiptNo.toString().contains(query)
            val matchesDate = dateFilter.isBlank() || r.receiptDate.contains(dateFilter)
            matchesFlatOrName && matchesDate
        }
    }

    val totalAmount = filtered.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search flat no. / name / receipt no.") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dateFilter,
            onValueChange = { dateFilter = it },
            label = { Text("Filter by date (dd, MM or dd/MM/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${filtered.size} receipts", fontWeight = FontWeight.SemiBold)
                Text("Total: ₹ ${"%.2f".format(totalAmount)}", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No receipts found", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { receipt ->
                    ReceiptListItem(receipt)
                }
            }
        }
    }
}

@Composable
private fun ReceiptListItem(receipt: Receipt) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Flat ${receipt.flatNumber} · ${receipt.residentName}", fontWeight = FontWeight.SemiBold)
                Text("Receipt #${receipt.receiptNo} · ${receipt.receiptDate}", style = MaterialTheme.typography.labelSmall)
                Text("${receipt.paymentMode} · ${receipt.accountOf}", style = MaterialTheme.typography.labelSmall)
            }
            Text("₹ ${"%.2f".format(receipt.amount)}", fontWeight = FontWeight.Bold)
        }
    }
}
