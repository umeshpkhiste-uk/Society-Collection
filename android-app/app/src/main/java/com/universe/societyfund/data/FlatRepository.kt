package com.universe.societyfund.data

import android.content.Context
import com.universe.societyfund.model.Flat
import org.json.JSONArray

/**
 * Loads the flat/owner directory bundled at app/src/main/assets/flats.json.
 * This was digitised from the Wing-E notice board photo. If flats change,
 * just edit that JSON file and rebuild - no code changes needed.
 */
object FlatRepository {

    private var cache: List<Flat>? = null

    fun getAllFlats(context: Context): List<Flat> {
        cache?.let { return it }

        val json = context.assets.open("flats.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = mutableListOf<Flat>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Flat(
                    number = obj.getString("number"),
                    wing = obj.optString("wing", "E"),
                    ownerName = obj.optString("name", "")
                )
            )
        }
        // Keep board order (already sorted by floor/unit in the JSON)
        cache = list
        return list
    }

    fun getWings(context: Context): List<String> =
        getAllFlats(context).map { it.wing }.distinct().sorted()
}
