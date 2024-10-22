package com.rafael.inclusimap.feature.libraryinfo.domain.repository

import com.rafael.inclusimap.feature.libraryinfo.domain.model.Contribution

interface ContributionsRepository {
    suspend fun addNewContribution(contribution: Contribution)
    suspend fun addNewContributions(contributions: List<Contribution>)
    suspend fun removeContribution(contribution: Contribution)
    suspend fun removeContributions(contributions: List<Contribution>)
}
