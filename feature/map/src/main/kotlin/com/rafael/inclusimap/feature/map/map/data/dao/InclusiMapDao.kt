package com.rafael.inclusimap.feature.map.map.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.feature.map.map.domain.InclusiMapEntity

@Dao
interface InclusiMapDao {

    @Query("SELECT * FROM inclusimap_db WHERE id = :id")
    suspend fun getPosition(id: Int): InclusiMapEntity?

    @Upsert
    suspend fun updatePosition(position: InclusiMapEntity)
}
