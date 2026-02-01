package com.rafael.inclusimap.core.navigation.types

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import com.rafael.inclusimap.core.util.map.model.Location
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

val locationType = object : NavType<Location?>(isNullableAllowed = true) {
    override fun get(bundle: SavedState, key: String): Location? {
        val jsonString = bundle.read { getString(key) }
        return json.decodeFromString(jsonString)
    }

    override fun put(bundle: SavedState, key: String, value: Location?) {
        bundle.write { putString(key, json.encodeToString(value)) }
    }

    override fun parseValue(value: String): Location = json.decodeFromString(value)

    override fun serializeAsValue(value: Location?): String = json.encodeToString(value)
}
