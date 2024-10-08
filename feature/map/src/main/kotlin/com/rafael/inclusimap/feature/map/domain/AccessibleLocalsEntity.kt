package com.rafael.inclusimap.feature.map.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker

@Entity(tableName = "accessible_locals")
data class AccessibleLocalsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val locals : List<AccessibleLocalMarker>
) {
    companion object {
        fun getDefault(): AccessibleLocalsEntity = AccessibleLocalsEntity(
            id = 1,
            locals = listOf()
        )
    }
}
