package com.universe.societyfund.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.universe.societyfund.model.Flat
import com.universe.societyfund.ui.theme.GreenPaid
import com.universe.societyfund.ui.theme.GreenPaidBg
import com.universe.societyfund.ui.theme.OrangeUnpaid
import com.universe.societyfund.ui.theme.OrangeUnpaidBg

@Composable
fun HomeScreen(
    flats: List<Flat>,
    paidFlatNumbers: Set<String>,
    onFlatSelected: (Flat) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, flats) {
        if (query.isBlank()) flats
        else flats.filter {
            it.number.contains(query, ignoreCase = true) ||
                it.ownerName.contains(query, ignoreCase = true)
        }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.floor }.toSortedMap() }

    val totalPaid = paidFlatNumbers.size
    val totalFlats = flats.size

    Column(modifier = Modifier.fillMaxSize()) {

        // Summary strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryChip(label = "Collected", value = "$totalPaid", color = GreenPaid, bg = GreenPaidBg)
            SummaryChip(label = "Pending", value = "${totalFlats - totalPaid}", color = OrangeUnpaid, bg = OrangeUnpaidBg)
            SummaryChip(label = "Total Flats", value = "$totalFlats", color = MaterialTheme.colorScheme.primary, bg = MaterialTheme.colorScheme.surfaceVariant)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search flat number or owner name") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grouped.forEach { (floor, flatsOnFloor) ->
                item(key = "header_$floor") {
                    Text(
                        text = if (floor == 0) "Ground / Other" else "Floor $floor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                item(key = "grid_$floor") {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 84.dp),
                        modifier = Modifier.height(((flatsOnFloor.size / 4 + 1) * 64).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(flatsOnFloor, key = { it.number }) { flat ->
                            FlatButton(
                                flat = flat,
                                isPaid = paidFlatNumbers.contains(flat.number),
                                onClick = { onFlatSelected(flat) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlatButton(flat: Flat, isPaid: Boolean, onClick: () -> Unit) {
    val bg = if (isPaid) GreenPaidBg else OrangeUnpaidBg
    val border = if (isPaid) GreenPaid else OrangeUnpaid

    Column(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = flat.number, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: androidx.compose.ui.graphics.Color, bg: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
        Text(text = label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
