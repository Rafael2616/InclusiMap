package com.rafael.inclusimap.feature.map.map.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accessible_locals")
data class AccessibleLocalsEntity(
    @PrimaryKey
    val id: Int,
    // Serialized List<AccessibleLocalMarker>
    val locals: String,
) {
    companion object {
        fun getDefault(): AccessibleLocalsEntity = AccessibleLocalsEntity(
            id = 1,
            locals = "[]",
        )
    }
}
