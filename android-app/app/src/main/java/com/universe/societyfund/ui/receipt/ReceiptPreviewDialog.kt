package com.universe.societyfund.ui.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.universe.societyfund.model.Receipt

/**
 * Shown right after a receipt is saved - lays the fields out the same way as
 * the "LIFE REPUBLIC SECTOR R10 / UNIVERSE SAHAKARI GRUHARACHNA SANSTHA
 * MARYADIT" printed receipt book, so it can be screenshotted/shared with the
 * resident as proof of payment.
 */
@Composable
fun ReceiptPreviewDialog(receipt: Receipt, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receipt No. ${receipt.receiptNo}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("LIFE REPUBLIC SECTOR R10 / 10th AVENUE", style = MaterialTheme.typography.labelSmall)
                Text("UNIVERSE SAHAKARI GRUHARACHNA SANSTHA MARYADIT", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(12.dp))
                ReceiptRow("Received with thanks from", receipt.residentName)
                ReceiptRow("Residents of", "Flat ${receipt.flatNumber}, Wing ${receipt.wing}")
                ReceiptRow("The sum of Rupees", "₹ ${"%.2f".format(receipt.amount)}")
                ReceiptRow("On account of", receipt.accountOf)
                ReceiptRow("Payment via", receipt.paymentMode)
                if (receipt.chequeOrRefNo.isNotBlank()) ReceiptRow("Cheque/Ref No.", receipt.chequeOrRefNo)
                if (receipt.bankName.isNotBlank()) ReceiptRow("Drawn on Bank", receipt.bankName)
                if (receipt.branch.isNotBlank()) ReceiptRow("Branch", receipt.branch)
                ReceiptRow("Date", receipt.receiptDate)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
