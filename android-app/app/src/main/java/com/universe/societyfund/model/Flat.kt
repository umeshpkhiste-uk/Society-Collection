package com.universe.societyfund.model

/**
 * Represents a single flat in the society directory board.
 * Loaded from assets/flats.json (edit that file to add/fix flats/owners).
 */
data class Flat(
    val number: String,
    val wing: String,
    val ownerName: String
) {
    /** Floor number derived from the flat number, e.g. "1203" -> 12 */
    val floor: Int
        get() = if (number.length <= 2) 0 else number.dropLast(2).toIntOrNull() ?: 0
}
