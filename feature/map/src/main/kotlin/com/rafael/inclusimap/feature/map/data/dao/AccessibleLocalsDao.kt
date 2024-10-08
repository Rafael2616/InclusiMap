package com.rafael.inclusimap.feature.map.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.map.domain.AccessibleLocalsEntity

@Dao
interface AccessibleLocalsDao {

    @Query("SELECT locals FROM accessible_locals WHERE id = :id")
    suspend fun getLocals(id: Int): List<AccessibleLocalMarker>

    @Upsert
    suspend fun updateLocals(localsEntity: AccessibleLocalsEntity)
}
