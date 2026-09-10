package com.universe.societyfund.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.universe.societyfund.model.Flat
import com.universe.societyfund.ui.home.HomeScreen
import com.universe.societyfund.ui.receipt.ReceiptFormScreen
import com.universe.societyfund.ui.receipt.ReceiptPreviewDialog
import com.universe.societyfund.ui.reports.ReportsScreen
import com.universe.societyfund.viewmodel.SocietyViewModel

private object Routes {
    const val HOME = "home"
    const val RECEIPT = "receipt"
    const val REPORTS = "reports"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val viewModel: SocietyViewModel = viewModel()
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.loadFlats(context)
        viewModel.startObservingReceipts()
    }

    val flats by viewModel.flats.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var selectedFlat by remember { mutableStateOf<Flat?>(null) }
    var successReceiptNo by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination

            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute?.hierarchy?.any { it.route == Routes.HOME } == true,
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Flats") }
                )
                NavigationBarItem(
                    selected = currentRoute?.hierarchy?.any { it.route == Routes.REPORTS } == true,
                    onClick = {
                        navController.navigate(Routes.REPORTS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    flats = flats,
                    paidFlatNumbers = viewModel.paidFlatNumbers(),
                    onFlatSelected = { flat ->
                        selectedFlat = flat
                        navController.navigate(Routes.RECEIPT)
                    }
                )
            }
            composable(Routes.RECEIPT) {
                val flat = selectedFlat
                if (flat == null) {
                    Text("Pick a flat from the Flats tab first.", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                } else {
                    ReceiptFormScreen(
                        flat = flat,
                        isSubmitting = isSubmitting,
                        onSubmit = { amount, accountOf, paymentMode, bankName, branch, chequeOrRefNo, receiptDate ->
                            viewModel.submitReceipt(
                                flat = flat,
                                amount = amount,
                                accountOf = accountOf,
                                paymentMode = paymentMode,
                                bankName = bankName,
                                branch = branch,
                                chequeOrRefNo = chequeOrRefNo,
                                receiptDate = receiptDate,
                                enteredBy = "Committee",
                                onSuccess = { receiptNo ->
                                    successReceiptNo = receiptNo
                                },
                                onError = { message ->
                                    errorMessage = message
                                }
                            )
                        }
                    )
                }
            }
            composable(Routes.REPORTS) {
                ReportsScreen(receipts = receipts)
            }
        }
    }

    // Success dialog
    val flatForSuccess = selectedFlat
    val successNo = successReceiptNo
    if (successNo != null && flatForSuccess != null) {
        val savedReceipt = receipts.firstOrNull { it.receiptNo == successNo }
        if (savedReceipt != null) {
            ReceiptPreviewDialog(
                receipt = savedReceipt,
                onDismiss = {
                    successReceiptNo = null
                    selectedFlat = null
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Couldn't save receipt") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }
}
