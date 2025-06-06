package com.rafael.inclusimap.feature.map.map.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rafael.inclusimap.core.domain.util.Constants.PARAGOMINAS_LAT_LNG

@Entity(tableName = "inclusimap_db")
data class InclusiMapEntity(
    @PrimaryKey
    val id: Int,
    val zoom: Float,
    val lat: Double,
    val lng: Double,
    val tilt: Float,
    val bearing: Float,
) {
    companion object {
        fun getDefault(): InclusiMapEntity = InclusiMapEntity(
            id = 1,
            zoom = 15f,
            lat = PARAGOMINAS_LAT_LNG.latitude,
            lng = PARAGOMINAS_LAT_LNG.longitude,
            tilt = 0f,
            bearing = 0f,
        )
    }
}
