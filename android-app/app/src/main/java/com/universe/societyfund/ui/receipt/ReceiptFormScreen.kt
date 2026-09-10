package com.universe.societyfund.ui.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.universe.societyfund.model.Flat
import java.text.SimpleDateFormat
import java.util.*

private val paymentModes = listOf("Cash", "Cheque", "NEFT", "Mygate")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptFormScreen(
    flat: Flat,
    isSubmitting: Boolean,
    onSubmit: (
        amount: Double,
        accountOf: String,
        paymentMode: String,
        bankName: String,
        branch: String,
        chequeOrRefNo: String,
        receiptDate: String
    ) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var accountOf by remember { mutableStateOf("Annual Maintenance Fund") }
    var paymentMode by remember { mutableStateOf(paymentModes[0]) }
    var bankName by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var chequeOrRefNo by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var receiptDate by remember { mutableStateOf(today) }
    var modeMenuExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull()
    val canSubmit = amountValue != null && amountValue > 0 && !isSubmitting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Flat / resident header, pulled from the directory board data
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Flat ${flat.number}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(flat.ownerName.ifBlank { "(name not on record)" }, style = MaterialTheme.typography.bodyLarge)
                Text("Wing ${flat.wing}", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount (₹)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = accountOf,
            onValueChange = { accountOf = it },
            label = { Text("On account of") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = modeMenuExpanded,
            onExpandedChange = { modeMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = paymentMode,
                onValueChange = {},
                readOnly = true,
                label = { Text("Payment via") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = modeMenuExpanded, onDismissRequest = { modeMenuExpanded = false }) {
                paymentModes.forEach { mode ->
                    DropdownMenuItem(text = { Text(mode) }, onClick = {
                        paymentMode = mode
                        modeMenuExpanded = false
                    })
                }
            }
        }

        if (paymentMode == "Cheque" || paymentMode == "NEFT") {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = chequeOrRefNo,
                onValueChange = { chequeOrRefNo = it },
                label = { Text(if (paymentMode == "Cheque") "Cheque No." else "Reference / UTR No.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Drawn on Bank") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = branch,
                onValueChange = { branch = it },
                label = { Text("Branch") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = receiptDate,
            onValueChange = { receiptDate = it },
            label = { Text("Date (dd/MM/yyyy)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSubmit(
                    amountValue ?: 0.0,
                    accountOf,
                    paymentMode,
                    bankName,
                    branch,
                    chequeOrRefNo,
                    receiptDate
                )
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Save Receipt", fontWeight = FontWeight.Bold)
            }
        }

        if (amountValue == null && amount.isNotBlank()) {
            Text(
                "Enter a valid amount",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            )
        }
    }
}
