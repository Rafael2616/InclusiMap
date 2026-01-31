package com.rafael.inclusimap.feature.contributions.domain.repository

import com.rafael.inclusimap.feature.contributions.domain.model.Contribution

interface ContributionsRepository {
    suspend fun addNewContributions(contributions: List<Contribution>)
    suspend fun removeContribution(contribution: Contribution)
    suspend fun removeContributions(contributions: List<Contribution>)
}
