package com.universe.societyfund.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Mirrors the fields on the "Life Republic / Universe Sahakari Gruharachna
 * Sanstha Maryadit" printed receipt book, so a digital receipt can be
 * generated/printed/shared in the same format.
 */
data class Receipt(
    val id: String = "",
    val receiptNo: Int = 0,
    val flatNumber: String = "",
    val wing: String = "",
    val residentName: String = "",
    val amount: Double = 0.0,
    val accountOf: String = "",          // e.g. "Annual Maintenance Fund 2026-27"
    val paymentMode: String = "",        // Cash / Cheque / NEFT / Mygate
    val bankName: String = "",
    val branch: String = "",
    val chequeOrRefNo: String = "",
    val receiptDate: String = "",        // dd/MM/yyyy, as typed by the collector
    @ServerTimestamp val createdAt: Date? = null,
    val enteredBy: String = ""
)
