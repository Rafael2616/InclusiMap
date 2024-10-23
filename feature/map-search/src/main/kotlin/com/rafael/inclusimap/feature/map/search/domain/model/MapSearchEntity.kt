package com.rafael.inclusimap.feature.map.search.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_search_db")
data class MapSearchEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    // Serialized List<String> of type placeID
    val placeHistory: String,
)
