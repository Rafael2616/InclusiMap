package com.rafael.inclusimap.feature.map.map.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rafael.inclusimap.feature.map.map.domain.AccessibleLocalsEntity

@Dao
interface AccessibleLocalsDao {

    @Query("SELECT * FROM accessible_locals WHERE id = :id")
    suspend fun getLocals(id: Int): AccessibleLocalsEntity?

    @Upsert
    suspend fun updateLocals(localsEntity: AccessibleLocalsEntity)
}
