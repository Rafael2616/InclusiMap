package com.rafael.inclusimap.domain.repository

import com.rafael.inclusimap.domain.AppIntroEntity

interface AppIntroRepository {
    suspend fun getAppIntro(id: Int): AppIntroEntity?
    suspend fun updateAppIntro(appIntroEntity: AppIntroEntity)
}
