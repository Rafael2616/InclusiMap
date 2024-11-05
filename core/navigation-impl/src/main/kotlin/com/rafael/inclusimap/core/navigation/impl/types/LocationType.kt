package com.rafael.inclusimap.core.navigation.impl.types

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.rafael.inclusimap.core.navigation.Location
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
}

val locationType = object : NavType<Location?>(
    isNullableAllowed = true,
) {
    override fun get(bundle: Bundle, key: String): Location? {
        return json.decodeFromString(bundle.getString(key) ?: return null)
    }

    override fun put(bundle: Bundle, key: String, value: Location?) {
        bundle.putString(key, json.encodeToString(value))
    }

    override fun parseValue(value: String): Location = json.decodeFromString(value)

    override fun serializeAsValue(value: Location?): String = Uri.encode(json.encodeToString(value))
}
