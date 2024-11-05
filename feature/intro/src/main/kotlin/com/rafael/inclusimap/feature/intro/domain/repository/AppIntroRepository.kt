package com.rafael.inclusimap.feature.intro.domain.repository

import com.rafael.inclusimap.feature.intro.domain.model.AppIntroEntity

interface AppIntroRepository {
    suspend fun getAppIntro(id: Int): AppIntroEntity?

    suspend fun updateAppIntro(appIntroEntity: AppIntroEntity)
}
