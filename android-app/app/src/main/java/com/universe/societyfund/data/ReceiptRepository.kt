package com.universe.societyfund.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.universe.societyfund.model.Receipt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * All receipts live in one Firestore collection ("receipts") so every
 * committee member's phone sees the same live data. Receipt numbers are
 * assigned centrally via a transaction on "counters/receiptCounter" so two
 * people entering receipts at the same time never collide.
 */
object ReceiptRepository {

    private val db = FirebaseFirestore.getInstance()
    private val receiptsCollection = db.collection("receipts")
    private val countersCollection = db.collection("counters")

    /** Live stream of every receipt, newest receipt number first. */
    fun observeReceipts(): Flow<List<Receipt>> = callbackFlow {
        val listener = receiptsCollection
            .orderBy("receiptNo", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val receipts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Receipt::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(receipts)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Atomically reserves the next receipt number.
     * Starting value (2477) matches the last used number in the physical
     * receipt book shown in Rec. No. 2478 - change this default in Firestore
     * (counters/receiptCounter/lastNumber) if your book has moved on.
     */
    suspend fun getNextReceiptNumber(): Int {
        val counterRef = countersCollection.document("receiptCounter")
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val current = snapshot.getLong("lastNumber") ?: 2477L
            val next = current + 1
            transaction.set(counterRef, mapOf("lastNumber" to next))
            next
        }.await().toInt()
    }

    suspend fun saveReceipt(receipt: Receipt) {
        receiptsCollection.add(receipt).await()
    }

    suspend fun getReceiptsForFlat(flatNumber: String): List<Receipt> {
        val snapshot = receiptsCollection.whereEqualTo("flatNumber", flatNumber).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Receipt::class.java)?.copy(id = it.id) }
    }
}
