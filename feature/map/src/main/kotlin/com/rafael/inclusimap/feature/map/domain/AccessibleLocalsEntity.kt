package com.rafael.inclusimap.feature.map.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accessible_locals")
data class AccessibleLocalsEntity(
    @PrimaryKey
    val id: Int,
    val locals : String // Serialized List<AccessibleLocalMarker>
) {
    companion object {
        fun getDefault(): AccessibleLocalsEntity = AccessibleLocalsEntity(
            id = 1,
            locals = "[]"
        )
    }
}
