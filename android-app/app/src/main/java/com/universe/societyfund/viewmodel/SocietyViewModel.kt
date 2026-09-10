package com.universe.societyfund.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universe.societyfund.data.FlatRepository
import com.universe.societyfund.data.ReceiptRepository
import com.universe.societyfund.model.Flat
import com.universe.societyfund.model.Receipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocietyViewModel : ViewModel() {

    private val _flats = MutableStateFlow<List<Flat>>(emptyList())
    val flats: StateFlow<List<Flat>> = _flats.asStateFlow()

    // Live, shared across every device via Firestore snapshot listener
    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private var receiptsListenerStarted = false

    fun loadFlats(context: Context) {
        if (_flats.value.isEmpty()) {
            _flats.value = FlatRepository.getAllFlats(context)
        }
    }

    fun startObservingReceipts() {
        if (receiptsListenerStarted) return
        receiptsListenerStarted = true
        viewModelScope.launch {
            ReceiptRepository.observeReceipts().collect { list ->
                _receipts.value = list
            }
        }
    }

    /** Flat numbers that have at least one receipt recorded - used to color the Home grid. */
    fun paidFlatNumbers(): Set<String> = _receipts.value.map { it.flatNumber }.toSet()

    fun submitReceipt(
        flat: Flat,
        amount: Double,
        accountOf: String,
        paymentMode: String,
        bankName: String,
        branch: String,
        chequeOrRefNo: String,
        receiptDate: String,
        enteredBy: String,
        onSuccess: (receiptNo: Int) -> Unit,
        onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val nextNo = ReceiptRepository.getNextReceiptNumber()
                val receipt = Receipt(
                    receiptNo = nextNo,
                    flatNumber = flat.number,
                    wing = flat.wing,
                    residentName = flat.ownerName,
                    amount = amount,
                    accountOf = accountOf,
                    paymentMode = paymentMode,
                    bankName = bankName,
                    branch = branch,
                    chequeOrRefNo = chequeOrRefNo,
                    receiptDate = receiptDate,
                    enteredBy = enteredBy
                )
                ReceiptRepository.saveReceipt(receipt)
                onSuccess(nextNo)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save receipt. Check your internet connection.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
